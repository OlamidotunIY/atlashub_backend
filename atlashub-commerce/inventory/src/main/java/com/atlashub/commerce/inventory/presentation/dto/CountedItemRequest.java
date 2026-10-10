package com.atlashub.commerce.inventory.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CountedItemRequest(
        @NotNull(message = "Inventory ID is required")
        Long inventoryId,

        @Min(value = 0, message = "Counted quantity must not be negative")
        int countedQuantity
) {
}
