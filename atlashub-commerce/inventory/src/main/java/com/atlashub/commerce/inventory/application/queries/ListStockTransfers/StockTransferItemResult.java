package com.atlashub.commerce.inventory.application.queries.ListStockTransfers;

public record StockTransferItemResult(
        Long id,
        Long productId,
        Integer quantityRequested,
        Integer quantityReceived
) {
}
