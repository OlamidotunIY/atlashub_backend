package com.atlashub.commerce.inventory.domain.entities;

import com.atlashub.commerce.inventory.domain.events.StockCountReconciledEvent;
import com.atlashub.commerce.inventory.domain.exceptions.InvalidInventoryOperationException;
import com.atlashub.commerce.inventory.domain.valueobject.StockCountStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
public class StockCount extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final Long outletId;
    private StockCountStatus status;
    private final List<StockCountItem> items;
    private final ZonedDateTime startedAt;
    private ZonedDateTime reconciledAt;

    public StockCount(Long id, Long organizationId, Long outletId, StockCountStatus status,
                      List<StockCountItem> items, ZonedDateTime startedAt, ZonedDateTime reconciledAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.outletId = outletId;
        this.status = status;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.startedAt = startedAt;
        this.reconciledAt = reconciledAt;
    }

    public static StockCount create(Long id, Long organizationId, Long outletId) {
        Objects.requireNonNull(id, "StockCount ID must not be null");
        Objects.requireNonNull(organizationId, "Organization ID must not be null");
        Objects.requireNonNull(outletId, "Outlet ID must not be null");

        return new StockCount(id, organizationId, outletId, StockCountStatus.OPEN, new ArrayList<>(),
                ZonedDateTime.now(), null);
    }

    public void addItem(Long itemId, Long inventoryId, Long productId, int systemQty) {
        if (this.status != StockCountStatus.OPEN) {
            throw new InvalidInventoryOperationException("Cannot add items to a reconciled stock count");
        }
        StockCountItem item = StockCountItem.create(itemId, this.id, inventoryId, productId, systemQty);
        this.items.add(item);
    }

    public void updateCount(Long inventoryId, int countedQty) {
        if (this.status != StockCountStatus.OPEN) {
            throw new InvalidInventoryOperationException("Cannot update count in a reconciled stock count");
        }
        StockCountItem item = items.stream()
                .filter(i -> i.getInventoryId().equals(inventoryId))
                .findFirst()
                .orElseThrow(() -> new InvalidInventoryOperationException(
                        "Item for inventory " + inventoryId + " not found in this stock count"
                ));
        item.recordCount(countedQty);
    }

    public void reconcile() {
        if (this.status != StockCountStatus.OPEN) {
            throw new InvalidInventoryOperationException("Stock count is already reconciled");
        }
        this.status = StockCountStatus.RECONCILED;
        this.reconciledAt = ZonedDateTime.now();
        registerEvent(StockCountReconciledEvent.of(id, organizationId, outletId));
    }

    public List<StockCountItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public Long getId() {
        return id;
    }
}
