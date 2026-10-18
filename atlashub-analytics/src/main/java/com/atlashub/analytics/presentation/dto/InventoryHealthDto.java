package com.atlashub.analytics.presentation.dto;

import java.math.BigDecimal;

public record InventoryHealthDto(
        Long productId,
        Long outletId,
        int currentStock,
        int reorderPoint,
        int stockCoverageDays,
        BigDecimal inventoryValue,
        boolean deadStock
) {}
