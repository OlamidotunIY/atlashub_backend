package com.atlashub.commerce.catalog.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PurchaseOrderItemRequest(
        @NotNull(message = "Product ID is required")
        Long productId,
        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity,
        @NotNull(message = "Unit cost is required")
        BigDecimal unitCost
) {
}
