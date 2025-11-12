package com.techcorp.employee.exception;

/**
 * Wyjątek rzucany przy problemach z zapisem pliku na dysku.
 * Zwraca status 500 Internal Server Error.
 */
public class FileStorageException extends RuntimeException {
    
    public FileStorageException(String message) {
        super(message);
    }
    
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
