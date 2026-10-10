package com.atlashub.commerce.inventory.application.queries.GetStockSummary;

public record StockSummaryResult(
        Long outletId,
        int totalSkus,
        int totalQuantity,
        int lowStockCount,
        int outOfStockCount
) {
}
