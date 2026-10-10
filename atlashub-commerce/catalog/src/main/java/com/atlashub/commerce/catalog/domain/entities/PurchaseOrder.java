package com.atlashub.commerce.catalog.domain.entities;

import com.atlashub.commerce.catalog.domain.events.PurchaseOrderReceivedEvent;
import com.atlashub.commerce.catalog.domain.events.PurchaseOrderSentEvent;
import com.atlashub.commerce.catalog.domain.exceptions.InvalidPurchaseOrderStateException;
import com.atlashub.commerce.catalog.domain.valueobject.PurchaseOrderStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class PurchaseOrder extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final Long outletId;
    private final Long supplierId;
    private PurchaseOrderStatus status;
    private Money totalAmount;
    private LocalDate expectedDeliveryDate;
    private final List<PurchaseOrderItem> items;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public PurchaseOrder(Long id, Long organizationId, Long outletId, Long supplierId,
                         PurchaseOrderStatus status, Money totalAmount, LocalDate expectedDeliveryDate,
                         List<PurchaseOrderItem> items, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.outletId = outletId;
        this.supplierId = supplierId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        validateInvariants();
    }

    public static PurchaseOrder create(Long id, Long organizationId, Long outletId, Long supplierId,
                                       LocalDate expectedDeliveryDate, CurrencyCode currency) {
        ZonedDateTime now = ZonedDateTime.now();
        Money initialTotal = new Money(BigDecimal.ZERO, currency != null ? currency : CurrencyCode.NGN);
        return new PurchaseOrder(id, organizationId, outletId, supplierId, PurchaseOrderStatus.DRAFT,
                initialTotal, expectedDeliveryDate, new ArrayList<>(), now, now);
    }

    public void addItem(Long itemId, Long productId, int quantity, Money unitCost) {
        if (this.status != PurchaseOrderStatus.DRAFT) {
            throw new InvalidPurchaseOrderStateException("Items can only be added to a draft purchase order");
        }
        PurchaseOrderItem item = PurchaseOrderItem.create(itemId, this.id, productId, quantity, unitCost);
        this.items.add(item);
        recalculateTotal();
        touch();
    }

    public void send() {
        if (this.status != PurchaseOrderStatus.DRAFT) {
            throw new InvalidPurchaseOrderStateException("Only draft purchase orders can be sent");
        }
        if (this.items.isEmpty()) {
            throw new InvalidPurchaseOrderStateException("Cannot send a purchase order with no items");
        }
        this.status = PurchaseOrderStatus.SENT;
        touch();
        registerEvent(new PurchaseOrderSentEvent(
                UUID.randomUUID().toString(),
                this.id,
                this.updatedAt,
                CorrelationId.getOrCreate(),
                new PurchaseOrderSentEvent.Payload(this.organizationId, this.outletId, this.supplierId,
                        this.totalAmount, this.expectedDeliveryDate, this.updatedAt)
        ));
    }

    public void receiveItems(Map<Long, Integer> receivedQuantities) {
        if (this.status != PurchaseOrderStatus.SENT && this.status != PurchaseOrderStatus.PARTIALLY_RECEIVED) {
            throw new InvalidPurchaseOrderStateException(
                    "Cannot receive items for purchase order with status: " + this.status);
        }
        if (receivedQuantities == null || receivedQuantities.isEmpty()) {
            throw new InvalidPurchaseOrderStateException("Received quantities map cannot be null or empty");
        }

        for (Map.Entry<Long, Integer> entry : receivedQuantities.entrySet()) {
            Long productId = entry.getKey();
            int qty = entry.getValue();
            PurchaseOrderItem item = this.items.stream()
                    .filter(i -> i.getProductId().equals(productId))
                    .findFirst()
                    .orElseThrow(() -> new InvalidPurchaseOrderStateException("Item not found in purchase order: " + productId));
            item.receive(qty);
        }

        boolean allFullyReceived = this.items.stream().allMatch(PurchaseOrderItem::isFullyReceived);
        if (allFullyReceived) {
            this.status = PurchaseOrderStatus.RECEIVED;
            touch();
            registerEvent(new PurchaseOrderReceivedEvent(
                    UUID.randomUUID().toString(),
                    this.id,
                    this.updatedAt,
                    CorrelationId.getOrCreate(),
                    new PurchaseOrderReceivedEvent.Payload(this.organizationId, this.outletId, this.supplierId,
                            this.totalAmount, this.updatedAt)
            ));
        } else {
            this.status = PurchaseOrderStatus.PARTIALLY_RECEIVED;
            touch();
        }
    }

    public void cancel() {
        if (this.status != PurchaseOrderStatus.DRAFT && this.status != PurchaseOrderStatus.SENT) {
            throw new InvalidPurchaseOrderStateException(
                    "Cannot cancel purchase order with status: " + this.status);
        }
        this.status = PurchaseOrderStatus.CANCELLED;
        touch();
    }

    public List<PurchaseOrderItem> getItems() {
        return Collections.unmodifiableList(this.items);
    }

    private void recalculateTotal() {
        if (this.items.isEmpty()) {
            CurrencyCode currency = this.totalAmount != null ? this.totalAmount.currency() : CurrencyCode.NGN;
            this.totalAmount = new Money(BigDecimal.ZERO, currency);
            return;
        }
        CurrencyCode currency = this.items.get(0).getUnitCost().currency();
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrderItem item : this.items) {
            BigDecimal itemTotal = item.getUnitCost().amount().multiply(BigDecimal.valueOf(item.getQuantityOrdered()));
            total = total.add(itemTotal);
        }
        this.totalAmount = new Money(total, currency);
    }

    private void validateInvariants() {
        if (id == null) {
            throw new InvalidPurchaseOrderStateException("Purchase order id cannot be null");
        }
        if (organizationId == null) {
            throw new InvalidPurchaseOrderStateException("Organization id cannot be null");
        }
        if (outletId == null) {
            throw new InvalidPurchaseOrderStateException("Outlet id cannot be null");
        }
        if (supplierId == null) {
            throw new InvalidPurchaseOrderStateException("Supplier id cannot be null");
        }
        if (status == null) {
            throw new InvalidPurchaseOrderStateException("Purchase order status cannot be null");
        }
        if (totalAmount == null) {
            throw new InvalidPurchaseOrderStateException("Total amount cannot be null");
        }
        if (createdAt == null || updatedAt == null) {
            throw new InvalidPurchaseOrderStateException("Timestamps cannot be null");
        }
    }

    private void touch() {
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
