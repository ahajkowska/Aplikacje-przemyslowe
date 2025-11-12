package com.techcorp.employee.exception;

/**
 * Wyjątek rzucany gdy żądany plik nie istnieje.
 * Zwraca status 404 Not Found.
 */
public class FileNotFoundException extends RuntimeException {
    
    public FileNotFoundException(String message) {
        super(message);
    }
    
    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
