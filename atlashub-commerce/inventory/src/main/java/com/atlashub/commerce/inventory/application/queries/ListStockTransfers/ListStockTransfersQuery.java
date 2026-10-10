package com.atlashub.commerce.inventory.application.queries.ListStockTransfers;

import com.atlashub.commerce.inventory.domain.valueobject.TransferStatus;

public record ListStockTransfersQuery(
        Long organizationId,
        TransferStatus status,
        int page,
        int size
) {
}
