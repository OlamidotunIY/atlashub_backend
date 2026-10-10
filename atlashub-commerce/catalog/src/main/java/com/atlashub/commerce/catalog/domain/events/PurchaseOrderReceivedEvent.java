package com.atlashub.commerce.catalog.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.ZonedDateTime;

public record PurchaseOrderReceivedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<PurchaseOrderReceivedEvent.Payload> {

    public record Payload(
            Long organizationId,
            Long outletId,
            Long supplierId,
            Money totalAmount,
            ZonedDateTime receivedAt
    ) {
    }
}
