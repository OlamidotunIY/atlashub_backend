package com.atlashub.main.exception;

import com.atlashub.shared.application.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void maps_data_integrity_violations_to_a_safe_conflict_response() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("duplicate database key"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().status());
        assertEquals("An account with these details already exists.", response.getBody().message());
    }
}
