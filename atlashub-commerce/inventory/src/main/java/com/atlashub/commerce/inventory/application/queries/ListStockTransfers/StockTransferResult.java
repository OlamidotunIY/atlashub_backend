package com.atlashub.commerce.inventory.application.queries.ListStockTransfers;

import com.atlashub.commerce.inventory.domain.valueobject.TransferStatus;

import java.time.ZonedDateTime;
import java.util.List;

public record StockTransferResult(
        Long id,
        Long organizationId,
        Long sourceOutletId,
        Long destinationOutletId,
        TransferStatus status,
        List<StockTransferItemResult> items,
        ZonedDateTime requestedAt,
        ZonedDateTime receivedAt
) {
}
