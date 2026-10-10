package com.atlashub.commerce.catalog.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record PurchaseOrderSentEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<PurchaseOrderSentEvent.Payload> {

    public record Payload(
            Long organizationId,
            Long outletId,
            Long supplierId,
            Money totalAmount,
            LocalDate expectedDeliveryDate,
            ZonedDateTime sentAt
    ) {
    }
}
