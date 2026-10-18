package com.atlashub.analytics.domain.entities;

import com.atlashub.analytics.domain.exceptions.AnalyticsInvariantException;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
public class InventoryHealthProjection extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final String environment;
    private final Long outletId;
    private final Long productId;
    private int currentStock;
    private int reorderPoint;
    private int stockCoverageDays;
    private BigDecimal inventoryValue;
    private boolean deadStock;
    private ZonedDateTime updatedAt;

    public InventoryHealthProjection(
            Long id,
            Long organizationId,
            String environment,
            Long outletId,
            Long productId,
            int currentStock,
            int reorderPoint,
            int stockCoverageDays,
            BigDecimal inventoryValue,
            boolean deadStock,
            ZonedDateTime updatedAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.environment = environment;
        this.outletId = outletId;
        this.productId = productId;
        this.currentStock = currentStock;
        this.reorderPoint = reorderPoint;
        this.stockCoverageDays = stockCoverageDays;
        this.inventoryValue = inventoryValue;
        this.deadStock = deadStock;
        this.updatedAt = updatedAt;
    }

    public static InventoryHealthProjection create(
            Long id,
            Long organizationId,
            String environment,
            Long outletId,
            Long productId
    ) {
        if (organizationId == null) {
            throw new AnalyticsInvariantException("Organization ID must not be null");
        }
        if (productId == null) {
            throw new AnalyticsInvariantException("Product ID must not be null");
        }
        return new InventoryHealthProjection(
                id,
                organizationId,
                environment != null ? environment : "live",
                outletId != null ? outletId : 0L,
                productId,
                0,
                0,
                0,
                BigDecimal.ZERO,
                false,
                ZonedDateTime.now()
        );
    }

    public void updateStock(int stock, int reorderPt, int coverageDays, BigDecimal value, boolean isDead) {
        this.currentStock = stock;
        this.reorderPoint = reorderPt;
        this.stockCoverageDays = coverageDays;
        this.inventoryValue = value != null ? value : BigDecimal.ZERO;
        this.deadStock = isDead;
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
