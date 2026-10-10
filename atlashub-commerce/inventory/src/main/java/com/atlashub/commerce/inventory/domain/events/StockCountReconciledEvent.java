package com.atlashub.commerce.inventory.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;
import java.util.UUID;

public record StockCountReconciledEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<StockCountReconciledEvent.Payload> {

    public record Payload(
            Long organizationId,
            Long outletId,
            ZonedDateTime reconciledAt
    ) {
    }

    public static StockCountReconciledEvent of(Long stockCountId, Long organizationId, Long outletId) {
        ZonedDateTime now = ZonedDateTime.now();
        return new StockCountReconciledEvent(
                UUID.randomUUID().toString(),
                stockCountId,
                now,
                UUID.randomUUID().toString(),
                new Payload(organizationId, outletId, now)
        );
    }
}
