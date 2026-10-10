package com.atlashub.commerce.inventory.presentation.dto;

public record InventoryLevelResponse(
        Long inventoryId,
        Long productId,
        Long outletId,
        int quantity,
        int reservedQuantity,
        int availableQuantity,
        int reorderLevel,
        int safeStock,
        boolean isLowStock
) {
}
