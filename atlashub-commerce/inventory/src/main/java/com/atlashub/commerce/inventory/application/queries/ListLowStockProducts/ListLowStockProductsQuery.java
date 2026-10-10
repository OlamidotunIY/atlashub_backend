package com.atlashub.commerce.inventory.application.queries.ListLowStockProducts;

public record ListLowStockProductsQuery(
        Long organizationId,
        Long outletId
) {
}
