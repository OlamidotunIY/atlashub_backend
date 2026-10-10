package com.atlashub.commerce.inventory.domain.entities;

import com.atlashub.commerce.inventory.domain.events.StockTransferApprovedEvent;
import com.atlashub.commerce.inventory.domain.events.StockTransferReceivedEvent;
import com.atlashub.commerce.inventory.domain.exceptions.InvalidTransferStateException;
import com.atlashub.commerce.inventory.domain.valueobject.TransferStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
public class StockTransfer extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final Long sourceOutletId;
    private final Long destinationOutletId;
    private TransferStatus status;
    private final List<StockTransferItem> items;
    private final ZonedDateTime requestedAt;
    private ZonedDateTime receivedAt;

    public StockTransfer(Long id, Long organizationId, Long sourceOutletId, Long destinationOutletId,
                         TransferStatus status, List<StockTransferItem> items,
                         ZonedDateTime requestedAt, ZonedDateTime receivedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.sourceOutletId = sourceOutletId;
        this.destinationOutletId = destinationOutletId;
        this.status = status;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.requestedAt = requestedAt;
        this.receivedAt = receivedAt;
    }

    public static StockTransfer create(Long id, Long organizationId, Long sourceOutletId, Long destinationOutletId) {
        Objects.requireNonNull(id, "Transfer ID must not be null");
        Objects.requireNonNull(organizationId, "Organization ID must not be null");
        Objects.requireNonNull(sourceOutletId, "Source outlet ID must not be null");
        Objects.requireNonNull(destinationOutletId, "Destination outlet ID must not be null");

        if (sourceOutletId.equals(destinationOutletId)) {
            throw new InvalidTransferStateException("Source and destination outlets must be different");
        }

        return new StockTransfer(id, organizationId, sourceOutletId, destinationOutletId,
                TransferStatus.REQUESTED, new ArrayList<>(), ZonedDateTime.now(), null);
    }

    public void addItem(Long itemId, Long productId, int quantityRequested) {
        if (this.status != TransferStatus.REQUESTED) {
            throw new InvalidTransferStateException("Cannot add items to transfer in state: " + status);
        }
        StockTransferItem item = StockTransferItem.create(itemId, this.id, productId, quantityRequested);
        this.items.add(item);
    }

    public void approve() {
        if (this.status != TransferStatus.REQUESTED) {
            throw new InvalidTransferStateException("Can only approve a transfer in REQUESTED state");
        }
        if (this.items.isEmpty()) {
            throw new InvalidTransferStateException("Cannot approve an empty stock transfer");
        }
        this.status = TransferStatus.APPROVED;
        registerEvent(StockTransferApprovedEvent.of(id, organizationId, sourceOutletId, destinationOutletId));
    }

    public void dispatch() {
        if (this.status != TransferStatus.APPROVED) {
            throw new InvalidTransferStateException("Can only dispatch an approved transfer");
        }
        this.status = TransferStatus.DISPATCHED;
    }

    public void receive(Map<Long, Integer> receivedQuantities) {
        if (this.status != TransferStatus.DISPATCHED) {
            throw new InvalidTransferStateException("Can only receive a dispatched transfer");
        }
        if (receivedQuantities != null) {
            for (StockTransferItem item : items) {
                Integer received = receivedQuantities.get(item.getProductId());
                item.recordReceived(received != null ? received : item.getQuantityRequested());
            }
        } else {
            for (StockTransferItem item : items) {
                item.recordReceived(item.getQuantityRequested());
            }
        }
        this.status = TransferStatus.RECEIVED;
        this.receivedAt = ZonedDateTime.now();
        registerEvent(StockTransferReceivedEvent.of(id, organizationId, sourceOutletId, destinationOutletId));
    }

    public void cancel() {
        if (this.status != TransferStatus.REQUESTED && this.status != TransferStatus.APPROVED) {
            throw new InvalidTransferStateException("Cannot cancel transfer in state: " + status);
        }
        this.status = TransferStatus.CANCELLED;
    }

    public List<StockTransferItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public Long getId() {
        return id;
    }
}
