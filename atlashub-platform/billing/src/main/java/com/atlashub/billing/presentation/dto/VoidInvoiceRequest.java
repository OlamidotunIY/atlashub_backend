package com.atlashub.billing.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record VoidInvoiceRequest(
        @NotBlank(message = "Void reason is required")
        String reason
) {
}
