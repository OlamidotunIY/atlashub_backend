package com.atlashub.commerce.inventory.application.queries.GetInventoryLevel;

public record GetInventoryLevelQuery(
        Long organizationId,
        Long outletId,
        Long productId,
        Long variantId
) {
}
