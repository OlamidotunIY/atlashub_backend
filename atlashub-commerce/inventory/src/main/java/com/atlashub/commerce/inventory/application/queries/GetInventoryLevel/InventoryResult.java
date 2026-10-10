package com.atlashub.commerce.inventory.application.queries.GetInventoryLevel;

public record InventoryResult(
        Long id,
        Long organizationId,
        Long outletId,
        Long productId,
        Long variantId,
        Integer quantity,
        Integer reservedQuantity,
        Integer availableQuantity,
        Integer reorderLevel,
        Integer safeStock,
        boolean isLowStock
) {
}
