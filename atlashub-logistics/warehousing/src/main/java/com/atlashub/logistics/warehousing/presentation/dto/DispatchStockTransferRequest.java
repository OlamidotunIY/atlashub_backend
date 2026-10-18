package com.atlashub.logistics.warehousing.presentation.dto;

import jakarta.validation.constraints.NotNull;

public record DispatchStockTransferRequest(
        @NotNull(message = "Shipment ID is required")
        Long shipmentId
) {}
