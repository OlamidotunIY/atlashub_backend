package com.atlashub.accounting.ap.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record DisputeInvoiceRequest(
        @NotBlank(message = "Dispute reason is required")
        String reason
) {}
