package com.atlashub.analytics.application.query.GetInventoryHealth;

import java.math.BigDecimal;

public record InventoryHealthResult(
        Long productId,
        Long outletId,
        int currentStock,
        int reorderPoint,
        int stockCoverageDays,
        BigDecimal inventoryValue,
        boolean deadStock
) {}
