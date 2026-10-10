package com.atlashub.commerce.inventory.domain.entities;

import com.atlashub.commerce.inventory.domain.events.CustomerReturnApprovedEvent;
import com.atlashub.commerce.inventory.domain.exceptions.InvalidReturnStateException;
import com.atlashub.commerce.inventory.domain.valueobject.RefundMethod;
import com.atlashub.commerce.inventory.domain.valueobject.ReturnStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
public class CustomerReturn extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final Long outletId;
    private final Long salesOrderId;
    private final Long customerId;
    private final Money refundAmount;
    private String reason;
    private final RefundMethod refundMethod;
    private ReturnStatus status;
    private final List<ReturnItem> items;
    private final ZonedDateTime createdAt;

    public CustomerReturn(Long id, Long organizationId, Long outletId, Long salesOrderId, Long customerId,
                          Money refundAmount, String reason, RefundMethod refundMethod,
                          ReturnStatus status, List<ReturnItem> items, ZonedDateTime createdAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.outletId = outletId;
        this.salesOrderId = salesOrderId;
        this.customerId = customerId;
        this.refundAmount = refundAmount;
        this.reason = reason;
        this.refundMethod = refundMethod;
        this.status = status;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.createdAt = createdAt;
    }

    public static CustomerReturn create(Long id, Long organizationId, Long outletId, Long salesOrderId,
                                        Long customerId, Money refundAmount, String reason,
                                        RefundMethod refundMethod) {
        Objects.requireNonNull(id, "Return ID must not be null");
        Objects.requireNonNull(organizationId, "Organization ID must not be null");
        Objects.requireNonNull(outletId, "Outlet ID must not be null");
        Objects.requireNonNull(salesOrderId, "SalesOrder ID must not be null");
        Objects.requireNonNull(refundAmount, "Refund amount must not be null");
        Objects.requireNonNull(refundMethod, "Refund method must not be null");

        return new CustomerReturn(id, organizationId, outletId, salesOrderId, customerId,
                refundAmount, reason, refundMethod, ReturnStatus.PENDING, new ArrayList<>(), ZonedDateTime.now());
    }

    public void addItem(Long itemId, Long productId, int quantity) {
        if (this.status != ReturnStatus.PENDING) {
            throw new InvalidReturnStateException("Cannot add items to return in state: " + status);
        }
        ReturnItem item = ReturnItem.create(itemId, this.id, productId, quantity);
        this.items.add(item);
    }

    public void approve() {
        if (this.status != ReturnStatus.PENDING) {
            throw new InvalidReturnStateException("Can only approve a return in PENDING state");
        }
        if (this.items.isEmpty()) {
            throw new InvalidReturnStateException("Cannot approve an empty return");
        }
        this.status = ReturnStatus.APPROVED;
        registerEvent(CustomerReturnApprovedEvent.of(id, organizationId, outletId, salesOrderId, customerId, refundAmount, refundMethod));
    }

    public void reject(String rejectionReason) {
        if (this.status != ReturnStatus.PENDING) {
            throw new InvalidReturnStateException("Can only reject a return in PENDING state");
        }
        this.status = ReturnStatus.REJECTED;
        this.reason = rejectionReason;
    }

    public List<ReturnItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public Long getId() {
        return id;
    }
}
