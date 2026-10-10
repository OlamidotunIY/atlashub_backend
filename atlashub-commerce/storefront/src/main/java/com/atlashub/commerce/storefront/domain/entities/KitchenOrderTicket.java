package com.atlashub.commerce.storefront.domain.entities;

import com.atlashub.commerce.storefront.domain.events.KitchenOrderTicketCreatedEvent;
import com.atlashub.commerce.storefront.domain.events.KotReadyEvent;
import com.atlashub.commerce.storefront.domain.exceptions.InvalidOrderStateException;
import com.atlashub.commerce.storefront.domain.valueobject.KotStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
public class KitchenOrderTicket extends AggregateRoot<Long> {

    private final Long id;
    private final Long salesOrderId;
    private final Long tableId;
    private final Long outletId;
    private final List<KotItem> items;
    private KotStatus status;
    private final ZonedDateTime sentAt;

    public KitchenOrderTicket(
            Long id,
            Long salesOrderId,
            Long tableId,
            Long outletId,
            List<KotItem> items,
            KotStatus status,
            ZonedDateTime sentAt
    ) {
        this.id = id;
        this.salesOrderId = salesOrderId;
        this.tableId = tableId;
        this.outletId = outletId;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.status = status;
        this.sentAt = sentAt;
    }

    public static KitchenOrderTicket create(
            Long id,
            Long salesOrderId,
            Long tableId,
            Long outletId,
            List<KotItem> initialItems
    ) {
        Objects.requireNonNull(id, "KOT ID must not be null");
        Objects.requireNonNull(salesOrderId, "Sales order ID must not be null");
        Objects.requireNonNull(outletId, "Outlet ID must not be null");
        if (initialItems == null || initialItems.isEmpty()) {
            throw new InvalidOrderStateException("KOT must have at least one item");
        }

        ZonedDateTime now = ZonedDateTime.now();
        KitchenOrderTicket kot = new KitchenOrderTicket(
                id,
                salesOrderId,
                tableId,
                outletId,
                new ArrayList<>(initialItems),
                KotStatus.PENDING,
                now
        );

        List<KitchenOrderTicketCreatedEvent.KotItemPayload> eventItems = kot.items.stream()
                .map(i -> new KitchenOrderTicketCreatedEvent.KotItemPayload(i.getProductId(), i.getName(), i.getQuantity()))
                .toList();
        kot.registerEvent(KitchenOrderTicketCreatedEvent.of(id, salesOrderId, outletId, tableId, eventItems));

        return kot;
    }

    public void addItem(KotItem item) {
        if (status != KotStatus.PENDING) {
            throw new InvalidOrderStateException("Cannot add items to KOT in status: " + status);
        }
        Objects.requireNonNull(item, "KOT item must not be null");
        this.items.add(item);
    }

    public void markInProgress() {
        if (status != KotStatus.PENDING) {
            throw new InvalidOrderStateException("Cannot mark in-progress for KOT in status: " + status);
        }
        this.status = KotStatus.IN_PROGRESS;
    }

    public void markReady() {
        if (status != KotStatus.IN_PROGRESS) {
            throw new InvalidOrderStateException("Cannot mark ready for KOT in status: " + status);
        }
        this.status = KotStatus.READY;
        registerEvent(KotReadyEvent.of(id, salesOrderId, outletId, tableId));
    }

    public void markServed() {
        if (status != KotStatus.READY) {
            throw new InvalidOrderStateException("Cannot mark served for KOT in status: " + status);
        }
        this.status = KotStatus.SERVED;
    }

    @Override
    public Long getId() {
        return id;
    }

    public List<KotItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
