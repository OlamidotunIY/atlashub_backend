package com.atlashub.logistics.returns.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectReturnShipmentRequest(
        @NotBlank(message = "Rejection reason is required")
        String reason
) {}
