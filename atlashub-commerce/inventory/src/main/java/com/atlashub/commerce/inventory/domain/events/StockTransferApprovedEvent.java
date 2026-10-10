package com.atlashub.commerce.inventory.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;
import java.util.UUID;

public record StockTransferApprovedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<StockTransferApprovedEvent.Payload> {

    public record Payload(
            Long organizationId,
            Long sourceOutletId,
            Long destinationOutletId,
            ZonedDateTime approvedAt
    ) {
    }

    public static StockTransferApprovedEvent of(Long transferId, Long organizationId,
                                                Long sourceOutletId, Long destinationOutletId) {
        ZonedDateTime now = ZonedDateTime.now();
        return new StockTransferApprovedEvent(
                UUID.randomUUID().toString(),
                transferId,
                now,
                UUID.randomUUID().toString(),
                new Payload(organizationId, sourceOutletId, destinationOutletId, now)
        );
    }
}
