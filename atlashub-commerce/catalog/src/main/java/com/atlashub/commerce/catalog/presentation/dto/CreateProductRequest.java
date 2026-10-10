package com.atlashub.commerce.catalog.presentation.dto;

import com.atlashub.commerce.catalog.domain.valueobject.ProductType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProductRequest(
        Long vendorId,
        @NotBlank(message = "Product code is required")
        String code,
        @NotBlank(message = "Product name is required")
        String name,
        String description,
        Long categoryId,
        Long departmentId,
        boolean taxable,
        boolean service,
        boolean hasVariants,
        @NotNull(message = "Product type is required")
        ProductType type
) {
}
