package com.atlashub.commerce.catalog.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateSupplierRequest(
        @NotBlank(message = "Supplier name is required")
        String name,
        String email,
        String phone,
        String address
) {
}
