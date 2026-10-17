package com.atlashub.analytics.application.query.GetInventoryValue;

import java.math.BigDecimal;

public record InventoryValueResult(
        Long outletId,
        int totalSkus,
        int totalStock,
        BigDecimal totalStockValue,
        int lowStockCount
) {}
