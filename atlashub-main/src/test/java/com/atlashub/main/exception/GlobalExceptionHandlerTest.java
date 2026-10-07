package com.atlashub.main.exception;

import com.atlashub.shared.application.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void keeps_unexpected_persistence_failures_generic() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleUnexpectedError(
                new IllegalStateException("persistence failure"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().status());
        assertEquals("An unexpected internal error occurred.", response.getBody().message());
    }
}
