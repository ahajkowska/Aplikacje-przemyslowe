package com.techcorp.employee.service;

import com.techcorp.employee.model.DocumentType;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.EmployeeDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Serwis do zarządzania dokumentami pracowników.
 * Przechowuje metadane dokumentów w pamięci (Map).
 */
@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    // Mapa przechowująca metadane dokumentów: documentId -> EmployeeDocument
    private final Map<String, EmployeeDocument> documentsStore = new ConcurrentHashMap<>();

    private final FileStorageService fileStorageService;
    private final EmployeeService employeeService;

    public DocumentService(FileStorageService fileStorageService, EmployeeService employeeService) {
        this.fileStorageService = fileStorageService;
        this.employeeService = employeeService;
    }

    /**
     * Zapisuje dokument pracownika.
     *
     * @param email email pracownika
     * @param file plik dokumentu
     * @param documentType typ dokumentu
     * @return metadane zapisanego dokumentu
     * @throws IOException jeśli wystąpi błąd zapisu
     * @throws IllegalArgumentException jeśli pracownik nie istnieje
     */
    public EmployeeDocument saveDocument(String email, MultipartFile file, DocumentType documentType) 
            throws IOException {
        
        // Walidacja czy pracownik istnieje
        Employee employee = employeeService.getAllEmployees().stream()
                .filter(e -> e.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Pracownik o emailu " + email + " nie istnieje"));

        // Walidacja pliku
        fileStorageService.validateFile(file);

        // Zapisz plik w podkatalogu documents/{email}/
        String savedFileName = fileStorageService.saveEmployeeDocument(email, file);
        
        // Pobierz pełną ścieżkę
        Path documentsPath = fileStorageService.getUploadLocation()
                .resolve("documents")
                .resolve(email);
        Path fullPath = documentsPath.resolve(savedFileName);

        // Utwórz metadane dokumentu
        EmployeeDocument document = new EmployeeDocument(
                email,
                savedFileName,
                file.getOriginalFilename(),
                documentType,
                fullPath.toString()
        );

        // Zapisz metadane w pamięci
        documentsStore.put(document.getId(), document);

        log.info("Zapisano dokument dla pracownika {}: {} (typ: {})", 
                email, savedFileName, documentType);

        return document;
    }

    /**
     * Pobiera wszystkie dokumenty pracownika.
     *
     * @param email email pracownika
     * @return lista metadanych dokumentów
     */
    public List<EmployeeDocument> getEmployeeDocuments(String email) {
        return documentsStore.values().stream()
                .filter(doc -> doc.getEmployeeEmail().equalsIgnoreCase(email))
                .sorted(Comparator.comparing(EmployeeDocument::getUploadDate).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Pobiera konkretny dokument po ID.
     *
     * @param documentId ID dokumentu
     * @return metadane dokumentu
     * @throws IllegalArgumentException jeśli dokument nie istnieje
     */
    public EmployeeDocument getDocument(String documentId) {
        EmployeeDocument document = documentsStore.get(documentId);
        if (document == null) {
            throw new IllegalArgumentException("Dokument o ID " + documentId + " nie istnieje");
        }
        return document;
    }

    /**
     * Pobiera dokument pracownika po emailu i ID dokumentu.
     *
     * @param email email pracownika
     * @param documentId ID dokumentu
     * @return metadane dokumentu
     * @throws IllegalArgumentException jeśli dokument nie istnieje lub nie należy do pracownika
     */
    public EmployeeDocument getEmployeeDocument(String email, String documentId) {
        EmployeeDocument document = getDocument(documentId);
        
        if (!document.getEmployeeEmail().equalsIgnoreCase(email)) {
            throw new IllegalArgumentException(
                    "Dokument " + documentId + " nie należy do pracownika " + email);
        }
        
        return document;
    }

    /**
     * Ładuje plik dokumentu jako Resource.
     *
     * @param documentId ID dokumentu
     * @return zasób pliku
     * @throws IOException jeśli wystąpi błąd odczytu
     */
    public Resource loadDocumentFile(String documentId) throws IOException {
        EmployeeDocument document = getDocument(documentId);
        Path filePath = Paths.get(document.getFilePath());
        
        return fileStorageService.loadFileFromPath(filePath);
    }

    /**
     * Usuwa dokument.
     *
     * @param email email pracownika
     * @param documentId ID dokumentu
     * @return true jeśli dokument został usunięty
     * @throws IOException jeśli wystąpi błąd usuwania
     */
    public boolean deleteDocument(String email, String documentId) throws IOException {
        EmployeeDocument document = getEmployeeDocument(email, documentId);
        
        // Usuń plik z dysku
        Path filePath = Paths.get(document.getFilePath());
        boolean deleted = Files.deleteIfExists(filePath);
        
        // Usuń metadane z pamięci
        documentsStore.remove(documentId);
        
        log.info("Usunięto dokument {} dla pracownika {}", documentId, email);
        
        return deleted;
    }

    /**
     * Pobiera liczbę dokumentów pracownika.
     *
     * @param email email pracownika
     * @return liczba dokumentów
     */
    public long getDocumentCount(String email) {
        return documentsStore.values().stream()
                .filter(doc -> doc.getEmployeeEmail().equalsIgnoreCase(email))
                .count();
    }

    /**
     * Pobiera wszystkie dokumenty (wszystkich pracowników).
     *
     * @return lista wszystkich dokumentów
     */
    public List<EmployeeDocument> getAllDocuments() {
        return new ArrayList<>(documentsStore.values());
    }
}
