package com.atlashub.commerce.catalog.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReceivedItemRequest(
        @NotNull(message = "Product ID is required")
        Long productId,
        @Min(value = 0, message = "Quantity received must be non-negative")
        int quantityReceived
) {
}
