package com.atlashub.logistics.warehousing.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;
import java.util.UUID;

public record StockTransferCancelledEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<StockTransferCancelledEvent.Payload> {

    public record Payload(
            Long stockTransferId,
            Long organizationId,
            String reason,
            ZonedDateTime cancelledAt
    ) {}

    public static StockTransferCancelledEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new StockTransferCancelledEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
