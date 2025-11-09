package com.techcorp.employee.exception;

/**
 * Wyjątek rzucany gdy próbujemy utworzyć pracownika z emailem, który już istnieje w systemie.
 */
public class DuplicateEmailException extends RuntimeException {
    
    public DuplicateEmailException(String email) {
        super("Employee with email '" + email + "' already exists");
    }

    public DuplicateEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
