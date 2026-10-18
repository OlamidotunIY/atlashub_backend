package com.atlashub.analytics.application.command.UpdateInventoryHealthProjection;

import java.math.BigDecimal;

public record UpdateInventoryHealthProjectionCommand(
        Long organizationId,
        String environment,
        Long outletId,
        Long productId,
        int currentStock,
        int reorderPoint,
        int coverageDays,
        BigDecimal inventoryValue,
        boolean deadStock
) {}
