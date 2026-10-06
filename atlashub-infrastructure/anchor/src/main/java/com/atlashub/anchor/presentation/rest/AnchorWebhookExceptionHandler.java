package com.atlashub.anchor.presentation.rest;

import com.atlashub.anchor.exception.InvalidAnchorWebhookSignatureException;
import com.atlashub.anchor.exception.MalformedAnchorWebhookException;
import com.atlashub.shared.application.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Returns a permanent client error for forged or malformed callbacks; transient failures remain 5xx. */
@RestControllerAdvice(assignableTypes = AnchorWebhookController.class)
public class AnchorWebhookExceptionHandler {

    @ExceptionHandler({InvalidAnchorWebhookSignatureException.class, MalformedAnchorWebhookException.class})
    ResponseEntity<ApiResponse<Void>> reject(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, "Invalid webhook request", null, null));
    }
}
