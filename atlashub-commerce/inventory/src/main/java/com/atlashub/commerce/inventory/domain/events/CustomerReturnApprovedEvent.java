package com.atlashub.commerce.inventory.domain.events;

import com.atlashub.commerce.inventory.domain.valueobject.RefundMethod;
import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.ZonedDateTime;
import java.util.UUID;

public record CustomerReturnApprovedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<CustomerReturnApprovedEvent.Payload> {

    public record Payload(
            Long organizationId,
            Long outletId,
            Long salesOrderId,
            Long customerId,
            Money refundAmount,
            RefundMethod refundMethod,
            ZonedDateTime approvedAt
    ) {
    }

    public static CustomerReturnApprovedEvent of(Long returnId, Long organizationId, Long outletId,
                                                Long salesOrderId, Long customerId,
                                                Money refundAmount, RefundMethod refundMethod) {
        ZonedDateTime now = ZonedDateTime.now();
        return new CustomerReturnApprovedEvent(
                UUID.randomUUID().toString(),
                returnId,
                now,
                UUID.randomUUID().toString(),
                new Payload(organizationId, outletId, salesOrderId, customerId, refundAmount, refundMethod, now)
        );
    }
}
