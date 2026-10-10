package com.atlashub.commerce.inventory.application.queries.ListLowStockProducts;

public record LowStockResult(
        Long id,
        Long organizationId,
        Long outletId,
        Long productId,
        Long variantId,
        Integer quantity,
        Integer reorderLevel,
        Integer safeStock
) {
}
