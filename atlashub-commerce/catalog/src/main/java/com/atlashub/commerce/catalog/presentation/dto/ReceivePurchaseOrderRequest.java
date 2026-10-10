package com.atlashub.commerce.catalog.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ReceivePurchaseOrderRequest(
        @NotEmpty(message = "Received items list cannot be empty")
        @Valid
        List<ReceivedItemRequest> receivedItems
) {
}
