package com.atlashub.commerce.inventory.presentation.dto;

import com.atlashub.commerce.inventory.domain.valueobject.AdjustmentReason;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AdjustStockRequest(
        @NotNull(message = "Inventory ID is required")
        Long inventoryId,

        @Min(value = 0, message = "New quantity must not be negative")
        int newQuantity,

        @NotNull(message = "Adjustment reason is required")
        AdjustmentReason reason
) {
}
