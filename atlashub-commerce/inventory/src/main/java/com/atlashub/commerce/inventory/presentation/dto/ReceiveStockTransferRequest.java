package com.atlashub.commerce.inventory.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ReceiveStockTransferRequest(
        @NotEmpty(message = "Received transfer items list must not be empty")
        List<@Valid ReceivedTransferItemRequest> items
) {
}
