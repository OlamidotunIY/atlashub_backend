package com.atlashub.commerce.inventory.domain.entities;

import com.atlashub.commerce.inventory.domain.valueobject.AdjustmentReason;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.Objects;

@Getter
public class StockAdjustment extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final Long inventoryId;
    private final Long adjustedBy;
    private final Integer previousQty;
    private final Integer newQty;
    private final AdjustmentReason reason;
    private final ZonedDateTime createdAt;

    public StockAdjustment(Long id, Long organizationId, Long inventoryId, Long adjustedBy,
                           Integer previousQty, Integer newQty, AdjustmentReason reason,
                           ZonedDateTime createdAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.inventoryId = inventoryId;
        this.adjustedBy = adjustedBy;
        this.previousQty = previousQty;
        this.newQty = newQty;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public static StockAdjustment create(Long id, Long organizationId, Long inventoryId, Long adjustedBy,
                                         Integer previousQty, Integer newQty, AdjustmentReason reason) {
        Objects.requireNonNull(id, "StockAdjustment ID must not be null");
        Objects.requireNonNull(organizationId, "Organization ID must not be null");
        Objects.requireNonNull(inventoryId, "Inventory ID must not be null");
        Objects.requireNonNull(reason, "AdjustmentReason must not be null");

        return new StockAdjustment(id, organizationId, inventoryId, adjustedBy, previousQty, newQty, reason, ZonedDateTime.now());
    }

    @Override
    public Long getId() {
        return id;
    }
}
