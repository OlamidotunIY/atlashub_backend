package com.atlashub.commerce.inventory.presentation.dto;

import com.atlashub.commerce.inventory.domain.valueobject.TransferStatus;

import java.time.ZonedDateTime;
import java.util.List;

public record StockTransferResponse(
        Long transferId,
        Long sourceOutletId,
        Long destinationOutletId,
        TransferStatus status,
        List<StockTransferItemResponse> items,
        ZonedDateTime requestedAt,
        ZonedDateTime receivedAt
) {
}
