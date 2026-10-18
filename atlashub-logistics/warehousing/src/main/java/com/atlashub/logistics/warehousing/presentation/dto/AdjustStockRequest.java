package com.atlashub.logistics.warehousing.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AdjustStockRequest(
        @NotNull(message = "Product ID is required")
        Long productId,

        @Min(value = 0, message = "New quantity must be non-negative")
        int newQuantity
) {}
