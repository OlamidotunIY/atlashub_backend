package com.atlashub.commerce.inventory.domain.entities;

import com.atlashub.commerce.inventory.domain.events.StockReservationFailedEvent;
import com.atlashub.commerce.inventory.domain.events.StockReservedEvent;
import com.atlashub.commerce.inventory.domain.exceptions.InvalidInventoryOperationException;
import com.atlashub.commerce.inventory.domain.valueobject.ReservationStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
public class StockReservation extends AggregateRoot<Long> {

    private final Long id;
    private final Long salesOrderId;
    private final Long organizationId;
    private final Long outletId;
    private final List<StockReservationItem> items;
    private ReservationStatus status;
    private String failureReason;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public StockReservation(Long id, Long salesOrderId, Long organizationId, Long outletId,
                            List<StockReservationItem> items, ReservationStatus status,
                            String failureReason, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.salesOrderId = salesOrderId;
        this.organizationId = organizationId;
        this.outletId = outletId;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.status = status;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static StockReservation create(Long id, Long salesOrderId, Long organizationId,
                                          Long outletId, List<StockReservationItem> items) {
        Objects.requireNonNull(id, "StockReservation ID must not be null");
        Objects.requireNonNull(salesOrderId, "SalesOrder ID must not be null");
        Objects.requireNonNull(organizationId, "Organization ID must not be null");
        Objects.requireNonNull(outletId, "Outlet ID must not be null");

        ZonedDateTime now = ZonedDateTime.now();
        return new StockReservation(
                id,
                salesOrderId,
                organizationId,
                outletId,
                items,
                ReservationStatus.ACTIVE,
                null,
                now,
                now
        );
    }

    public void confirm() {
        if (this.status != ReservationStatus.ACTIVE) {
            throw new InvalidInventoryOperationException("Cannot confirm reservation in status " + this.status);
        }
        if (this.items.isEmpty()) {
            throw new InvalidInventoryOperationException("Cannot confirm reservation with no items");
        }
        List<StockReservedEvent.ReservedItemDto> eventItems = this.items.stream()
                .map(item -> new StockReservedEvent.ReservedItemDto(item.getProductId(), item.getQuantity()))
                .toList();

        this.updatedAt = ZonedDateTime.now();
        registerEvent(StockReservedEvent.of(id, salesOrderId, organizationId, outletId, eventItems));
    }

    public void fail(String reason) {
        if (this.status != ReservationStatus.ACTIVE) {
            throw new InvalidInventoryOperationException("Cannot fail reservation in status " + this.status);
        }
        this.status = ReservationStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = ZonedDateTime.now();
        registerEvent(StockReservationFailedEvent.of(id, salesOrderId, organizationId, outletId, reason));
    }

    public void fulfill() {
        if (this.status != ReservationStatus.ACTIVE) {
            throw new InvalidInventoryOperationException("Cannot fulfill reservation in status " + this.status);
        }
        this.status = ReservationStatus.FULFILLED;
        this.updatedAt = ZonedDateTime.now();
    }

    public void release() {
        if (this.status != ReservationStatus.ACTIVE) {
            throw new InvalidInventoryOperationException("Cannot release reservation in status " + this.status);
        }
        this.status = ReservationStatus.RELEASED;
        this.updatedAt = ZonedDateTime.now();
    }

    public List<StockReservationItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public Long getId() {
        return id;
    }
}
