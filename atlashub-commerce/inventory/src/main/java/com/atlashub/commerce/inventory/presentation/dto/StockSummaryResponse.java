package com.atlashub.commerce.inventory.presentation.dto;

public record StockSummaryResponse(
        Long outletId,
        int totalSkus,
        int totalQuantity,
        int lowStockCount,
        int outOfStockCount
) {
}
