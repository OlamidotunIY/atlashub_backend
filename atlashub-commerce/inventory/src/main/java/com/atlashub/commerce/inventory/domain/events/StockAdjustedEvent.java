package com.atlashub.commerce.inventory.domain.events;

import com.atlashub.commerce.inventory.domain.valueobject.AdjustmentReason;
import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;
import java.util.UUID;

public record StockAdjustedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<StockAdjustedEvent.Payload> {

    public record Payload(
            Long organizationId,
            Long outletId,
            Long productId,
            int previousQty,
            int newQty,
            AdjustmentReason reason,
            Long adjustedBy,
            ZonedDateTime adjustedAt
    ) {
    }

    public static StockAdjustedEvent of(Long inventoryId, Long organizationId, Long outletId, Long productId,
                                        int previousQty, int newQty, AdjustmentReason reason, Long adjustedBy) {
        ZonedDateTime now = ZonedDateTime.now();
        return new StockAdjustedEvent(
                UUID.randomUUID().toString(),
                inventoryId,
                now,
                UUID.randomUUID().toString(),
                new Payload(organizationId, outletId, productId, previousQty, newQty, reason, adjustedBy, now)
        );
    }
}
