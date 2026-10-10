package com.atlashub.commerce.inventory.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RequestStockTransferRequest(
        @NotNull(message = "Source outlet ID is required")
        Long sourceOutletId,

        @NotNull(message = "Destination outlet ID is required")
        Long destinationOutletId,

        @NotEmpty(message = "Transfer items list must not be empty")
        List<@Valid TransferItemRequest> items
) {
}
