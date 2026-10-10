package com.atlashub.commerce.inventory.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReceivedTransferItemRequest(
        @NotNull(message = "Product ID is required")
        Long productId,

        @Min(value = 0, message = "Quantity received must not be negative")
        int quantityReceived
) {
}
