package com.atlashub.app.exception;

import com.atlashub.shared.exception.AtlasHubException;
import com.atlashub.shared.exception.ConflictException;
import com.atlashub.shared.exception.NotFoundException;
import com.atlashub.shared.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex);
    }
    
    @ExceptionHandler(com.atlashub.shared.exception.RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitException(com.atlashub.shared.exception.RateLimitExceededException ex) {
        ErrorResponse response = new ErrorResponse(
                ex.getErrorCodeString(),
                ex.getMessage(),
                ZonedDateTime.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
                .header("X-RateLimit-Limit", String.valueOf(ex.getLimit()))
                .header("X-RateLimit-Remaining", "0")
                .body(response);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(ConflictException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(com.atlashub.shared.exception.AuthorizationException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationException(com.atlashub.shared.exception.AuthorizationException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex);
    }

    @ExceptionHandler(AtlasHubException.class)
    public ResponseEntity<ErrorResponse> handleAtlasHubException(AtlasHubException ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        ErrorResponse response = new ErrorResponse(
                "VALIDATION_FAILED",
                "Input validation failed",
                ZonedDateTime.now(),
                errors
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse response = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                ZonedDateTime.now(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, AtlasHubException ex) {
        ErrorResponse response = new ErrorResponse(
                ex.getErrorCodeString(),
                ex.getMessage(),
                ZonedDateTime.now(),
                null
        );
        return new ResponseEntity<>(response, status);
    }

    public record ErrorResponse(
            String errorCode,
            String message,
            ZonedDateTime timestamp,
            Map<String, String> details
    ) {}
}
