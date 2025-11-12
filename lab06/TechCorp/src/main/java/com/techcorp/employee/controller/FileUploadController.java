package com.techcorp.employee.controller;

import com.techcorp.employee.model.DocumentType;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.EmployeeDocument;
import com.techcorp.employee.model.ImportSummary;
import com.techcorp.employee.service.DocumentService;
import com.techcorp.employee.service.EmployeeService;
import com.techcorp.employee.service.FileStorageService;
import com.techcorp.employee.service.ImportService;
import com.techcorp.employee.service.ReportGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Kontroler obsługujący upload i import plików CSV i XML.
 */
@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

    private final FileStorageService fileStorageService;
    private final ImportService importService;
    private final ReportGeneratorService reportGeneratorService;
    private final DocumentService documentService;
    private final EmployeeService employeeService;

    public FileUploadController(FileStorageService fileStorageService, 
                               ImportService importService,
                               ReportGeneratorService reportGeneratorService,
                               DocumentService documentService,
                               EmployeeService employeeService) {
        this.fileStorageService = fileStorageService;
        this.importService = importService;
        this.reportGeneratorService = reportGeneratorService;
        this.documentService = documentService;
        this.employeeService = employeeService;
    }

    /**
     Endpoint do importu pracowników z pliku CSV.
     POST /api/files/import/csv
     @param file plik CSV z danymi pracowników
     @return podsumowanie importu ze szczegółami
     */
    @PostMapping("/import/csv")
    public ResponseEntity<?> importCsv(@RequestParam("file") MultipartFile file) {
        log.info("Otrzymano żądanie importu CSV: {}", file.getOriginalFilename());

        try {
            // Walidacja pliku
            validateCsvFile(file);
            
            // Zapisz plik w katalogu uploads
            String savedFilename = fileStorageService.saveUploadedFile(file);
            log.info("Plik CSV zapisany jako: {}", savedFilename);

            // Pobierz pełną ścieżkę do zapisanego pliku
            Path uploadedFilePath = fileStorageService.getUploadLocation().resolve(savedFilename);
            
            // Wykonaj import danych
            ImportSummary summary = importService.importFromCsv(uploadedFilePath.toString());
            
            log.info("Import CSV zakończony. Zaimportowano: {}, Błędy: {}", 
                    summary.getImportedCount(), summary.getErrors().size());

            // Zwróć wynik
            if (summary.getErrors().isEmpty()) {
                return ResponseEntity.ok(new ImportResponse(
                    true,
                    "Import zakończony sukcesem",
                    summary.getImportedCount(),
                    0,
                    summary.getErrors()
                ));
            } else {
                return ResponseEntity.ok(new ImportResponse(
                    true,
                    "Import zakończony z błędami",
                    summary.getImportedCount(),
                    summary.getErrors().size(),
                    summary.getErrors()
                ));
            }

        } catch (IllegalArgumentException e) {
            log.error("Błąd walidacji pliku CSV: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(
                "Błąd walidacji pliku",
                e.getMessage()
            ));
        } catch (IOException e) {
            log.error("Błąd podczas zapisu pliku CSV: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(
                "Błąd zapisu pliku",
                "Nie udało się zapisać pliku: " + e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Nieoczekiwany błąd podczas importu CSV: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(
                "Błąd importu",
                "Wystąpił nieoczekiwany błąd: " + e.getMessage()
            ));
        }
    }

    /**
     Endpoint do importu pracowników z pliku XML.
     POST /api/files/import/xml
     @param file plik XML z danymi pracowników
     @return podsumowanie importu ze szczegółami
     */
    @PostMapping("/import/xml")
    public ResponseEntity<?> importXml(@RequestParam("file") MultipartFile file) {
        log.info("Otrzymano żądanie importu XML: {}", file.getOriginalFilename());

        try {
            // Walidacja pliku
            validateXmlFile(file);
            
            // Zapisz plik w katalogu uploads
            String savedFilename = fileStorageService.saveUploadedFile(file);
            log.info("Plik XML zapisany jako: {}", savedFilename);

            // Pobierz pełną ścieżkę do zapisanego pliku
            Path uploadedFilePath = fileStorageService.getUploadLocation().resolve(savedFilename);
            
            // Wykonaj import danych
            ImportSummary summary = importService.importFromXml(uploadedFilePath.toString());
            
            log.info("Import XML zakończony. Zaimportowano: {}, Błędy: {}", 
                    summary.getImportedCount(), summary.getErrors().size());

            // Zwróć wynik
            if (summary.getErrors().isEmpty()) {
                return ResponseEntity.ok(new ImportResponse(
                    true,
                    "Import zakończony sukcesem",
                    summary.getImportedCount(),
                    0,
                    summary.getErrors()
                ));
            } else {
                return ResponseEntity.ok(new ImportResponse(
                    true,
                    "Import zakończony z błędami",
                    summary.getImportedCount(),
                    summary.getErrors().size(),
                    summary.getErrors()
                ));
            }

        } catch (IllegalArgumentException e) {
            log.error("Błąd walidacji pliku XML: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(
                "Błąd walidacji pliku",
                e.getMessage()
            ));
        } catch (IOException e) {
            log.error("Błąd podczas zapisu pliku XML: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(
                "Błąd zapisu pliku",
                "Nie udało się zapisać pliku: " + e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Nieoczekiwany błąd podczas importu XML: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(
                "Błąd importu",
                "Wystąpił nieoczekiwany błąd: " + e.getMessage()
            ));
        }
    }

    /**
     * Waliduje plik CSV.
     */
    private void validateCsvFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Plik nie może być pusty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("Plik musi mieć rozszerzenie .csv");
        }

        // Wykorzystaj walidację z FileStorageService
        fileStorageService.validateFile(file);
    }

    /**
     * Waliduje plik XML.
     */
    private void validateXmlFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Plik nie może być pusty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xml")) {
            throw new IllegalArgumentException("Plik musi mieć rozszerzenie .xml");
        }

        // Wykorzystaj walidację z FileStorageService
        fileStorageService.validateFile(file);
    }

    /**
     Endpoint do generowania i pobierania raportu CSV ze wszystkimi pracownikami.
     Opcjonalnie można podać parametr company, aby wygenerować raport dla konkretnej firmy.

     GET /api/files/export/csv
     GET /api/files/export/csv?company=TechCorp

     @param company opcjonalna nazwa firmy (null = wszyscy pracownicy)
     @return plik CSV jako zasób do pobrania
     */
    @GetMapping("/export/csv")
    public ResponseEntity<Resource> exportCsv(@RequestParam(required = false) String company) {
        log.info("Generowanie raportu CSV{}",
                company != null ? " dla firmy: " + company : " - wszyscy pracownicy");

        try {
            byte[] csvContent;
            String filename;

            if (company != null && !company.trim().isEmpty()) {
                csvContent = reportGeneratorService.generateCompanyCsvReport(company);
                filename = "employees_" + company.replaceAll("[^a-zA-Z0-9]", "_") + ".csv";
            } else {
                csvContent = reportGeneratorService.generateAllEmployeesCsvReport();
                filename = "employees_all.csv";
            }

            ByteArrayResource resource = new ByteArrayResource(csvContent);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .contentLength(csvContent.length)
                    .body(resource);

        } catch (Exception e) {
            log.error("Błąd podczas generowania raportu CSV: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     Endpoint do generowania i pobierania raportu PDF ze statystykami firmy.

     GET /api/files/reports/statistics/{companyName}

     @param companyName nazwa firmy
     @return plik PDF jako zasób do pobrania
     */
    @GetMapping("/reports/statistics/{companyName}")
    public ResponseEntity<Resource> getCompanyStatisticsReport(@PathVariable String companyName) {
        log.info("Generowanie raportu PDF ze statystykami dla firmy: {}", companyName);

        try {
            byte[] pdfContent = reportGeneratorService.generateCompanyStatisticsPdfReport(companyName);
            String filename = "statistics_" + companyName.replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";

            ByteArrayResource resource = new ByteArrayResource(pdfContent);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfContent.length)
                    .body(resource);

        } catch (IllegalArgumentException e) {
            log.error("Firma nie istnieje: {}", companyName);
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("Błąd podczas generowania raportu PDF: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Nieoczekiwany błąd podczas generowania raportu PDF: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== ENDPOINTY DO ZARZĄDZANIA DOKUMENTAMI PRACOWNIKÓW ==========

    /**
     Endpoint do przesyłania dokumentu pracownika.

     POST /api/files/documents/{email}

     @param email email pracownika
     @param file plik dokumentu
     @param type typ dokumentu (CONTRACT, CERTIFICATE, ID_CARD, OTHER)
     @return metadane zapisanego dokumentu ze statusem 201 Created
     */
    @PostMapping("/documents/{email}")
    public ResponseEntity<?> uploadEmployeeDocument(
            @PathVariable String email,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") DocumentType type) {
        
        log.info("Przesyłanie dokumentu dla pracownika: {} (typ: {})", email, type);

        try {
            EmployeeDocument document = documentService.saveDocument(email, file, type);
            
            log.info("Dokument zapisany: {} dla pracownika: {}", document.getId(), email);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(document);

        } catch (IllegalArgumentException e) {
            log.error("Błąd walidacji: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(
                "Błąd walidacji",
                e.getMessage()
            ));
        } catch (IOException e) {
            log.error("Błąd podczas zapisu dokumentu: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(
                "Błąd zapisu pliku",
                "Nie udało się zapisać dokumentu: " + e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Nieoczekiwany błąd: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(
                "Błąd serwera",
                "Wystąpił nieoczekiwany błąd: " + e.getMessage()
            ));
        }
    }

    /**
     Endpoint do pobierania listy dokumentów pracownika.

     GET /api/files/documents/{email}

     @param email email pracownika
     @return lista metadanych dokumentów pracownika
     */
    @GetMapping("/documents/{email}")
    public ResponseEntity<List<EmployeeDocument>> getEmployeeDocuments(@PathVariable String email) {
        log.info("Pobieranie listy dokumentów dla pracownika: {}", email);

        try {
            List<EmployeeDocument> documents = documentService.getEmployeeDocuments(email);
            
            log.info("Znaleziono {} dokumentów dla pracownika: {}", documents.size(), email);
            
            return ResponseEntity.ok(documents);

        } catch (Exception e) {
            log.error("Błąd podczas pobierania dokumentów: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     Endpoint do pobierania konkretnego dokumentu pracownika.

     GET /api/files/documents/{email}/{documentId}

     @param email email pracownika
     @param documentId ID dokumentu
     @return plik dokumentu jako zasób do pobrania
     */
    @GetMapping("/documents/{email}/{documentId}")
    public ResponseEntity<Resource> downloadEmployeeDocument(
            @PathVariable String email,
            @PathVariable String documentId) {
        
        log.info("Pobieranie dokumentu {} dla pracownika: {}", documentId, email);

        try {
            // Pobierz metadane dokumentu i zweryfikuj czy należy do pracownika
            EmployeeDocument document = documentService.getEmployeeDocument(email, documentId);
            
            // Załaduj plik
            Resource resource = documentService.loadDocumentFile(documentId);

            // Określ Content-Type na podstawie rozszerzenia pliku
            String contentType = determineContentType(document.getOriginalFileName());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + document.getOriginalFileName() + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (IllegalArgumentException e) {
            log.error("Dokument nie znaleziony: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("Błąd podczas odczytu dokumentu: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Nieoczekiwany błąd: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint do usuwania dokumentu pracownika.
     * 
     * DELETE /api/files/documents/{email}/{documentId}
     * 
     * @param email email pracownika
     * @param documentId ID dokumentu
     * @return status 204 No Content jeśli usunięto pomyślnie
     */
    @DeleteMapping("/documents/{email}/{documentId}")
    public ResponseEntity<Void> deleteEmployeeDocument(
            @PathVariable String email,
            @PathVariable String documentId) {
        
        log.info("Usuwanie dokumentu {} dla pracownika: {}", documentId, email);

        try {
            documentService.deleteDocument(email, documentId);
            
            log.info("Dokument {} został usunięty", documentId);
            
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            log.error("Dokument nie znaleziony: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("Błąd podczas usuwania dokumentu: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Nieoczekiwany błąd: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== ENDPOINTY DO ZARZĄDZANIA ZDJĘCIAMI PROFILOWYMI ==========

    /**
     * Endpoint do przesyłania zdjęcia profilowego pracownika.
     * Zdjęcie musi być w formacie JPG lub PNG i nie przekraczać 2 MB.
     * 
     * POST /api/files/photos/{email}
     * 
     * @param email email pracownika
     * @param file plik zdjęcia
     * @return potwierdzenie zapisu ze statusem 200 OK
     */
    @PostMapping("/photos/{email}")
    public ResponseEntity<?> uploadEmployeePhoto(
            @PathVariable String email,
            @RequestParam("file") MultipartFile file) {
        
        log.info("Przesyłanie zdjęcia profilowego dla pracownika: {}", email);

        try {
            // Sprawdź czy pracownik istnieje
            Employee employee = employeeService.getAllEmployees().stream()
                    .filter(e -> e.getEmail().equalsIgnoreCase(email))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Pracownik o emailu " + email + " nie istnieje"));

            // Zapisz zdjęcie
            String photoFileName = fileStorageService.saveEmployeePhoto(email, file);
            
            // Aktualizuj pole photoFileName w obiekcie pracownika
            employee.setPhotoFileName(photoFileName);
            
            log.info("Zdjęcie profilowe zapisane: {} dla pracownika: {}", photoFileName, email);
            
            return ResponseEntity.ok(new PhotoUploadResponse(
                    true,
                    "Zdjęcie profilowe zostało zapisane",
                    photoFileName,
                    email
            ));

        } catch (IllegalArgumentException e) {
            log.error("Błąd walidacji: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(
                "Błąd walidacji",
                e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Błąd podczas przesyłania zdjęcia: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(
                "Błąd serwera",
                "Nie udało się zapisać zdjęcia: " + e.getMessage()
            ));
        }
    }

    /**
     * Endpoint do pobierania zdjęcia profilowego pracownika.
     * 
     * GET /api/files/photos/{email}
     * 
     * @param email email pracownika
     * @return zdjęcie jako zasób z odpowiednim Content-Type
     */
    @GetMapping("/photos/{email}")
    public ResponseEntity<Resource> getEmployeePhoto(@PathVariable String email) {
        log.info("Pobieranie zdjęcia profilowego dla pracownika: {}", email);

        try {
            // Sprawdź czy pracownik istnieje i ma zdjęcie
            Employee employee = employeeService.getAllEmployees().stream()
                    .filter(e -> e.getEmail().equalsIgnoreCase(email))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Pracownik o emailu " + email + " nie istnieje"));

            String photoFileName = employee.getPhotoFileName();
            
            if (photoFileName == null || photoFileName.isEmpty()) {
                log.warn("Pracownik {} nie ma zdjęcia profilowego", email);
                return ResponseEntity.notFound().build();
            }

            // Załaduj zdjęcie
            Resource resource = fileStorageService.loadEmployeePhoto(photoFileName);

            // Określ Content-Type na podstawie rozszerzenia
            String contentType = photoFileName.toLowerCase().endsWith(".png") 
                    ? "image/png" 
                    : "image/jpeg";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "inline; filename=\"" + photoFileName + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (IllegalArgumentException e) {
            log.error("Pracownik nie znaleziony: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Błąd podczas pobierania zdjęcia: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Pomocnicza metoda do określania typu MIME na podstawie rozszerzenia pliku.
     */
    private String determineContentType(String filename) {
        if (filename == null) {
            return "application/octet-stream";
        }
        
        String lowerFilename = filename.toLowerCase();
        if (lowerFilename.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerFilename.endsWith(".doc") || lowerFilename.endsWith(".docx")) {
            return "application/msword";
        } else if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFilename.endsWith(".png")) {
            return "image/png";
        } else if (lowerFilename.endsWith(".txt")) {
            return "text/plain";
        } else if (lowerFilename.endsWith(".csv")) {
            return "text/csv";
        } else if (lowerFilename.endsWith(".xml")) {
            return "application/xml";
        } else {
            return "application/octet-stream";
        }
    }

    /**
     * Klasa odpowiedzi dla przesyłania zdjęcia profilowego.
     */
    public static class PhotoUploadResponse {
        private boolean success;
        private String message;
        private String photoFileName;
        private String employeeEmail;

        public PhotoUploadResponse(boolean success, String message, 
                                  String photoFileName, String employeeEmail) {
            this.success = success;
            this.message = message;
            this.photoFileName = photoFileName;
            this.employeeEmail = employeeEmail;
        }

        // Gettery
        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getPhotoFileName() {
            return photoFileName;
        }

        public String getEmployeeEmail() {
            return employeeEmail;
        }
    }

    /**
     * Klasa odpowiedzi dla udanego importu.
     */
    public static class ImportResponse {
        private boolean success;
        private String message;
        private int importedCount;
        private int errorCount;
        private java.util.List<String> errors;

        public ImportResponse(boolean success, String message, int importedCount, 
                            int errorCount, java.util.List<String> errors) {
            this.success = success;
            this.message = message;
            this.importedCount = importedCount;
            this.errorCount = errorCount;
            this.errors = errors;
        }

        // Gettery
        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public int getImportedCount() {
            return importedCount;
        }

        public int getErrorCount() {
            return errorCount;
        }

        public java.util.List<String> getErrors() {
            return errors;
        }
    }

    /**
     * Klasa odpowiedzi dla błędów.
     */
    public static class ErrorResponse {
        private String error;
        private String message;

        public ErrorResponse(String error, String message) {
            this.error = error;
            this.message = message;
        }

        // Gettery
        public String getError() {
            return error;
        }

        public String getMessage() {
            return message;
        }
    }
}
