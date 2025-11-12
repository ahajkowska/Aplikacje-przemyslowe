package com.techcorp.employee.service;

import com.techcorp.employee.exception.FileNotFoundException;
import com.techcorp.employee.exception.FileStorageException;
import com.techcorp.employee.exception.InvalidFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 Serwis do zarządzania plikami - zapis, odczyt, usuwanie i walidacja plików.
 */
@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    // Dozwolone rozszerzenia plików
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".csv", ".txt", ".pdf", ".xlsx", ".xls", ".json", ".xml"
    );

    // Maksymalny rozmiar pliku w bajtach (10 MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    // Maksymalny rozmiar zdjęcia profilowego (2 MB)
    private static final long MAX_PHOTO_SIZE = 2 * 1024 * 1024;

    // Dozwolone rozszerzenia dla zdjęć
    private static final List<String> ALLOWED_PHOTO_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png");

    private final Path uploadLocation;
    private final Path reportsLocation;

    /**
    Konstruktor z lokalizacjami katalogów z application.properties
    */
    public FileStorageService(
            @Value("${app.upload.directory:uploads/}") String uploadDirectory,
            @Value("${app.reports.directory:reports/}") String reportsDirectory) {
        this.uploadLocation = Paths.get(uploadDirectory).toAbsolutePath().normalize();
        this.reportsLocation = Paths.get(reportsDirectory).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.uploadLocation);
            Files.createDirectories(this.reportsLocation);
            log.info("Zainicjalizowano FileStorageService:");
            log.info("- Katalog uploadów: {}", this.uploadLocation);
            log.info("- Katalog raportów: {}", this.reportsLocation);
        } catch (IOException e) {
            log.error("Nie można utworzyć katalogów dla plików", e);
            throw new FileStorageException("Nie można zainicjalizować magazynu plików", e);
        }
    }

    /**
     Zapisuje plik przesłany przez użytkownika do katalogu uploads.

     @param file plik do zapisania
     @return unikalna nazwa zapisanego pliku
     @throws IOException jeśli wystąpi błąd podczas zapisu
     @throws IllegalArgumentException jeśli plik nie przejdzie walidacji
     */
    public String saveUploadedFile(MultipartFile file) throws IOException {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("Nazwa pliku nie może być pusta");
        }

        // Generuj unikalną nazwę pliku
        String uniqueFilename = generateUniqueFilename(originalFilename);
        Path targetLocation = this.uploadLocation.resolve(uniqueFilename);

        // Zapisz plik
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        log.info("Zapisano plik: {} (rozmiar: {} bajtów)", uniqueFilename, file.getSize());

        return uniqueFilename;
    }

    /**
     Zapisuje plik do katalogu raportów.

     @param filename nazwa pliku
     @param content zawartość pliku
     @return nazwa zapisanego pliku
     @throws IOException jeśli wystąpi błąd podczas zapisu
     */
    public String saveReportFile(String filename, byte[] content) throws IOException {
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("Nazwa pliku nie może być pusta");
        }

        String uniqueFilename = generateUniqueFilename(filename);
        Path targetLocation = this.reportsLocation.resolve(uniqueFilename);

        Files.write(targetLocation, content);
        log.info("Zapisano raport: {} (rozmiar: {} bajtów)", uniqueFilename, content.length);

        return uniqueFilename;
    }

    /**
    Odczytuje plik z katalogu uploads jako Resource.
    
    @param filename nazwa pliku do odczytania
    @throws IOException jeśli plik nie istnieje lub nie można go odczytać
     */
    public Resource loadUploadedFile(String filename) throws IOException {
        return loadFile(this.uploadLocation, filename);
    }

    /**
    Odczytuje plik z katalogu raportów jako Resource.
    
    @param filename nazwa pliku do odczytania
    @throws IOException jeśli plik nie istnieje lub nie można go odczytać
     */
    public Resource loadReportFile(String filename) throws IOException {
        return loadFile(this.reportsLocation, filename);
    }

    /**
    Metoda do ładowania pliku z podanej lokalizacji.
    */
    private Resource loadFile(Path location, String filename) throws IOException {
        try {
            // Normalizuj nazwę pliku i sprawdź czy nie wychodzi poza katalog (bezpieczeństwo)
            Path filePath = location.resolve(filename).normalize();

            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                log.debug("Odczytano plik: {}", filename);
                return resource;
            } else {
                throw new IOException("Plik nie istnieje lub nie można go odczytać: " + filename);
            }
        } catch (Exception e) {
            log.error("Błąd podczas odczytu pliku: {}", filename, e);
            throw new IOException("Nie można odczytać pliku: " + filename, e);
        }
    }

    /**
    Usuwa plik z katalogu uploads.
    
    @param filename nazwa pliku do usunięcia
    @return true jeśli plik został usunięty, false jeśli plik nie istniał
    */
    public boolean deleteUploadedFile(String filename) throws IOException {
        return deleteFile(this.uploadLocation, filename);
    }

    /**
    Usuwa plik z katalogu raportów.
    
    @param filename nazwa pliku do usunięcia
    @return true jeśli plik został usunięty, false jeśli plik nie istniał
    */
    public boolean deleteReportFile(String filename) throws IOException {
        return deleteFile(this.reportsLocation, filename);
    }

    /**
    Metoda do usuwania pliku z podanej lokalizacji.
    */
    private boolean deleteFile(Path location, String filename) throws IOException {
        try {
            Path filePath = location.resolve(filename).normalize();

            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("Usunięto plik: {}", filename);
            } else {
                log.warn("Plik nie istnieje: {}", filename);
            }
            return deleted;
        } catch (Exception e) {
            log.error("Błąd podczas usuwania pliku: {}", filename, e);
            throw new IOException("Nie można usunąć pliku: " + filename, e);
        }
    }

    /**
    Waliduje plik, czy spełnia wymagania (rozszerzenia i rozmiar).
    
    @param file plik do walidacji
    */
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Plik nie może być pusty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new InvalidFileException("Nazwa pliku nie może być pusta");
        }

        // Walidacja rozszerzenia
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidFileException(
                    "Niedozwolone rozszerzenie pliku: " + extension + 
                    ". Dozwolone rozszerzenia: " + ALLOWED_EXTENSIONS
            );
        }

        // Walidacja rozmiaru
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException(
                    String.format("Plik jest za duży (%d bajtów). Maksymalny rozmiar: %d bajtów (10 MB)",
                            file.getSize(), MAX_FILE_SIZE)
            );
        }

        log.debug("Walidacja pliku zakończona pomyślnie: {} ({} bajtów)", 
                originalFilename, file.getSize());
    }

    /**
    Waliduje plik zdjęcia pod kątem formatu i rozmiaru.
     
    @param file plik zdjęcia do walidacji
    @throws InvalidFileException jeśli plik nie spełnia wymagań
    */
    public void validatePhotoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Plik nie może być pusty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new InvalidFileException("Nazwa pliku nie może być pusta");
        }

        // Walidacja rozszerzenia
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_PHOTO_EXTENSIONS.contains(extension)) {
            throw new InvalidFileException(
                    "Niedozwolony format zdjęcia: " + extension + 
                    ". Dozwolone formaty: JPG, JPEG, PNG"
            );
        }

        // Walidacja rozmiaru (max 2 MB dla zdjęć)
        if (file.getSize() > MAX_PHOTO_SIZE) {
            throw new InvalidFileException(
                    String.format("Zdjęcie jest za duże (%.2f MB). Maksymalny rozmiar: 2 MB",
                            file.getSize() / (1024.0 * 1024.0))
            );
        }

        log.debug("Walidacja zdjęcia zakończona pomyślnie: {} ({} bajtów)", 
                originalFilename, file.getSize());
    }

    /**
    Generuje unikalną nazwę pliku dodając UUID przed rozszerzeniem.
     
    @param originalFilename oryginalna nazwa pliku
    @return unikalna nazwa pliku
    */
    private String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String baseName = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
        
        // Usuń niebezpieczne znaki z nazwy pliku
        baseName = baseName.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        // Generuj unikalne ID
        String uniqueId = UUID.randomUUID().toString();
        return baseName + "_" + uniqueId + extension;
    }

    /**
    Pobiera rozszerzenie pliku.
     
    @param filename nazwa pliku
    @return rozszerzenie pliku lub pusty string jeśli brak rozszerzenia
    */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }

    /**
    Zwraca ścieżkę do katalogu uploads.
    */
    public Path getUploadLocation() {
        return uploadLocation;
    }

    /**
    Zwraca ścieżkę do katalogu raportów.
    */
    public Path getReportsLocation() {
        return reportsLocation;
    }

    /**
    Zapisuje dokument pracownika w podkatalogu documents/{email}/.
    
    @param employeeEmail email pracownika
    @param file plik do zapisania
    @return unikalna nazwa zapisanego pliku
    @throws IOException jeśli wystąpi błąd podczas zapisu
    */
    public String saveEmployeeDocument(String employeeEmail, MultipartFile file) throws IOException {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("Nazwa pliku nie może być pusta");
        }

        // Utwórz katalog documents/{email}/ jeśli nie istnieje
        Path documentsPath = this.uploadLocation.resolve("documents").resolve(employeeEmail);
        Files.createDirectories(documentsPath);

        // Generuj unikalną nazwę pliku
        String uniqueFilename = generateUniqueFilename(originalFilename);
        Path targetLocation = documentsPath.resolve(uniqueFilename);

        // Zapisz plik
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        log.info("Zapisano dokument pracownika {}: {} (rozmiar: {} bajtów)", 
                employeeEmail, uniqueFilename, file.getSize());

        return uniqueFilename;
    }

    /**
     * Ładuje plik z podanej ścieżki jako Resource.
     *
     * @param filePath pełna ścieżka do pliku
     * @return zasób reprezentujący plik
     * @throws IOException jeśli plik nie istnieje lub nie można go odczytać
     */
    public Resource loadFileFromPath(Path filePath) throws IOException {
        try {
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                log.debug("Odczytano plik: {}", filePath.getFileName());
                return resource;
            } else {
                throw new IOException("Plik nie istnieje lub nie można go odczytać: " + filePath);
            }
        } catch (Exception e) {
            log.error("Błąd podczas odczytu pliku: {}", filePath, e);
            throw new IOException("Nie można odczytać pliku: " + filePath, e);
        }
    }

    /**
     * Zapisuje zdjęcie profilowe pracownika.
     * Nazwa pliku to email pracownika z odpowiednim rozszerzeniem.
     *
     * @param employeeEmail email pracownika
     * @param file plik zdjęcia
     * @return nazwa zapisanego pliku
     * @throws IOException jeśli wystąpi błąd podczas zapisu
     */
    public String saveEmployeePhoto(String employeeEmail, MultipartFile file) throws IOException {
        validatePhotoFile(file);

        // Utwórz katalog photos/ jeśli nie istnieje
        Path photosPath = this.uploadLocation.resolve("photos");
        Files.createDirectories(photosPath);

        // Określ rozszerzenie na podstawie typu MIME
        String extension = getPhotoExtensionFromMimeType(file.getContentType());
        
        // Nazwa pliku to email (bezpieczna nazwa)
        String safeEmail = employeeEmail.replaceAll("[^a-zA-Z0-9@._-]", "_");
        String fileName = safeEmail + extension;
        
        Path targetLocation = photosPath.resolve(fileName);

        try {
            // Zapisz plik (nadpisz jeśli istnieje)
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("Zapisano zdjęcie profilowe dla: {} (rozmiar: {} bajtów)", 
                    employeeEmail, file.getSize());
            return fileName;
        } catch (IOException e) {
            log.error("Błąd podczas zapisu zdjęcia: {}", e.getMessage(), e);
            throw new FileStorageException("Nie można zapisać zdjęcia profilowego", e);
        }
    }

    /**
     * Ładuje zdjęcie profilowe pracownika.
     *
     * @param photoFileName nazwa pliku zdjęcia
     * @return zasób reprezentujący zdjęcie
     * @throws IOException jeśli plik nie istnieje
     */
    public Resource loadEmployeePhoto(String photoFileName) throws IOException {
        Path photosPath = this.uploadLocation.resolve("photos");
        Path photoPath = photosPath.resolve(photoFileName).normalize();

        if (!photoPath.startsWith(photosPath)) {
            throw new SecurityException("Próba dostępu do pliku poza dozwolonym katalogiem");
        }

        try {
            Resource resource = new UrlResource(photoPath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                log.debug("Odczytano zdjęcie: {}", photoFileName);
                return resource;
            } else {
                throw new FileNotFoundException("Zdjęcie nie istnieje: " + photoFileName);
            }
        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
                throw (FileNotFoundException) e;
            }
            log.error("Błąd podczas odczytu zdjęcia: {}", photoFileName, e);
            throw new IOException("Nie można odczytać zdjęcia: " + photoFileName, e);
        }
    }

    /**
     * Usuwa zdjęcie profilowe pracownika.
     *
     * @param photoFileName nazwa pliku zdjęcia
     * @return true jeśli usunięto
     * @throws IOException jeśli wystąpi błąd
     */
    public boolean deleteEmployeePhoto(String photoFileName) throws IOException {
        Path photosPath = this.uploadLocation.resolve("photos");
        Path photoPath = photosPath.resolve(photoFileName).normalize();

        if (!photoPath.startsWith(photosPath)) {
            throw new SecurityException("Próba usunięcia pliku poza dozwolonym katalogiem");
        }

        try {
            boolean deleted = Files.deleteIfExists(photoPath);
            if (deleted) {
                log.info("Usunięto zdjęcie: {}", photoFileName);
            }
            return deleted;
        } catch (IOException e) {
            log.error("Błąd podczas usuwania zdjęcia: {}", photoFileName, e);
            throw new FileStorageException("Nie można usunąć zdjęcia", e);
        }
    }

    /**
     * Określa rozszerzenie pliku na podstawie typu MIME.
     */
    private String getPhotoExtensionFromMimeType(String mimeType) {
        if (mimeType == null) {
            return ".jpg";
        }
        
        return switch (mimeType.toLowerCase()) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            default -> ".jpg";
        };
    }
}
