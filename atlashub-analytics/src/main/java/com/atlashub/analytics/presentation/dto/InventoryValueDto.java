package com.atlashub.analytics.presentation.dto;

import java.math.BigDecimal;

public record InventoryValueDto(
        Long outletId,
        int totalSkus,
        int totalStock,
        BigDecimal totalStockValue,
        int lowStockCount
) {}
