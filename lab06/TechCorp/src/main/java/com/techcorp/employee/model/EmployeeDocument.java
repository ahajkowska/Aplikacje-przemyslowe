package com.techcorp.employee.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Model reprezentujący dokument pracownika.
 * Metadane dokumentów są przechowywane w pamięci (bez JPA).
 */
public class EmployeeDocument {
    
    private String id;
    private String employeeEmail;
    private String fileName;              // Unikalna nazwa pliku na dysku
    private String originalFileName;      // Oryginalna nazwa pliku przesłana przez użytkownika
    private DocumentType fileType;
    private LocalDateTime uploadDate;
    private String filePath;              // Pełna ścieżka do pliku

    public EmployeeDocument() {
        this.id = UUID.randomUUID().toString();
        this.uploadDate = LocalDateTime.now();
    }

    public EmployeeDocument(String employeeEmail, String fileName, String originalFileName, 
                           DocumentType fileType, String filePath) {
        this();
        this.employeeEmail = employeeEmail;
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.fileType = fileType;
        this.filePath = filePath;
    }

    // Gettery i settery
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public DocumentType getFileType() {
        return fileType;
    }

    public void setFileType(DocumentType fileType) {
        this.fileType = fileType;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        return "EmployeeDocument{" +
                "id='" + id + '\'' +
                ", employeeEmail='" + employeeEmail + '\'' +
                ", fileName='" + fileName + '\'' +
                ", originalFileName='" + originalFileName + '\'' +
                ", fileType=" + fileType +
                ", uploadDate=" + uploadDate +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
