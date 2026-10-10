package com.atlashub.commerce.inventory.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ReconcileStockCountRequest(
        @NotEmpty(message = "Counted items list must not be empty")
        List<@Valid CountedItemRequest> items
) {
}
