package com.techcorp.employee.exception;

/**
 * Wyjątek rzucany gdy plik nie spełnia wymagań dotyczących typu lub rozmiaru.
 * Zwraca status 400 Bad Request.
 */
public class InvalidFileException extends RuntimeException {
    
    public InvalidFileException(String message) {
        super(message);
    }
    
    public InvalidFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
