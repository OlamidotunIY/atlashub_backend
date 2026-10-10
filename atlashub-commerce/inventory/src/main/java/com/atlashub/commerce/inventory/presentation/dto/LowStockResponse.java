package com.atlashub.commerce.inventory.presentation.dto;

public record LowStockResponse(
        Long inventoryId,
        Long productId,
        Long variantId,
        Long outletId,
        int quantity,
        int reorderLevel,
        int safeStock
) {
}
