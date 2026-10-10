package com.atlashub.commerce.catalog.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record SuspendVendorRequest(
        @NotBlank(message = "Suspension reason is required")
        String reason
) {
}
