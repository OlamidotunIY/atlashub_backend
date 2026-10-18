package com.atlashub.analytics.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StockAdjustedPayload(
        String eventId,
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
