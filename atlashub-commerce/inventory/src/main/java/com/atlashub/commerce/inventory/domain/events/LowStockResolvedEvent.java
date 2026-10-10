package com.atlashub.commerce.inventory.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;
import java.util.UUID;

public record LowStockResolvedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<LowStockResolvedEvent.Payload> {

    public record Payload(
            Long organizationId,
            Long outletId,
            Long productId,
            int quantity,
            int reorderLevel,
            ZonedDateTime resolvedAt
    ) {
    }

    public static LowStockResolvedEvent of(Long inventoryId, Long organizationId, Long outletId,
                                           Long productId, int quantity, int reorderLevel) {
        ZonedDateTime now = ZonedDateTime.now();
        return new LowStockResolvedEvent(
                UUID.randomUUID().toString(),
                inventoryId,
                now,
                UUID.randomUUID().toString(),
                new Payload(organizationId, outletId, productId, quantity, reorderLevel, now)
        );
    }
}
