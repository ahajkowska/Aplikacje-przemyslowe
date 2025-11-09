package com.techcorp.employee.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object dla standardowej odpowiedzi błędu.
 * Używany do zwracania informacji o błędach w API w sposób spójny.
 */
public class ErrorResponse {
    private String message;
    private LocalDateTime timestamp;
    private Integer status;
    private String path;

    public ErrorResponse() {
        // Domyślny konstruktor wymagany przez Jackson do deserializacji JSON
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String message, Integer status, String path) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.path = path;
    }

    public ErrorResponse(String message, LocalDateTime timestamp, Integer status, String path) {
        this.message = message;
        this.timestamp = timestamp;
        this.status = status;
        this.path = path;
    }

    // Gettery i settery

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", status=" + status +
                ", path='" + path + '\'' +
                '}';
    }
}
