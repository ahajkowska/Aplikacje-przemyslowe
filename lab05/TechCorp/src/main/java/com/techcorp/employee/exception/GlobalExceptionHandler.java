package com.techcorp.employee.exception;

import com.techcorp.employee.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Globalny handler wyjątków dla aplikacji REST API.
 * Przechwytuje wyjątki i konwertuje je na odpowiednie odpowiedzi HTTP z obiektem ErrorResponse.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Obsługuje wyjątek EmployeeNotFoundException.
     * @return 404 Not Found z obiektem ErrorResponse
     */
    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmployeeNotFoundException(
            EmployeeNotFoundException ex,
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            ex.getMessage(),
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Obsługuje wyjątek DuplicateEmailException.
     * @return 409 Conflict gdy próbujemy utworzyć pracownika z istniejącym emailem
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmailException(
            DuplicateEmailException ex,
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            ex.getMessage(),
            LocalDateTime.now(),
            HttpStatus.CONFLICT.value(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Obsługuje wyjątek InvalidDataException.
     * @return 400 Bad Request dla błędów walidacji danych
     */
    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDataException(
            InvalidDataException ex,
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            ex.getMessage(),
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Obsługuje wyjątek IllegalArgumentException.
     * @return 400 Bad Request dla nieprawidłowych argumentów
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            ex.getMessage(),
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Catch-all handler dla wszystkich nieobsłużonych wyjątków.
     * @return 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            "An internal server error occurred: " + ex.getMessage(),
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
