package com.atlashub.paystack.presentation.rest;

import com.atlashub.paystack.exception.InvalidPaystackWebhookSignatureException;
import com.atlashub.paystack.exception.MalformedPaystackWebhookException;
import com.atlashub.shared.application.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = PaystackWebhookController.class)
public class PaystackWebhookExceptionHandler {
    @ExceptionHandler(InvalidPaystackWebhookSignatureException.class)
    ResponseEntity<ApiResponse<Void>> invalidSignature(InvalidPaystackWebhookSignatureException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(false, ex.getMessage(), null, null));
    }

    @ExceptionHandler({MalformedPaystackWebhookException.class, IllegalArgumentException.class})
    ResponseEntity<ApiResponse<Void>> badRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, ex.getMessage(), null, null));
    }
}
