package com.techcorp.employee.exception;

/**
 * Wyjątek rzucany gdy pracownik o podanym identyfikatorze nie został znaleziony.
 */
public class EmployeeNotFoundException extends RuntimeException {
    
    public EmployeeNotFoundException(String message) {
        super(message);
    }

    public EmployeeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
