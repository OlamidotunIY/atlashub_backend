package com.atlashub.logistics.warehousing.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;
import java.util.UUID;

public record StockTransferDispatchedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<StockTransferDispatchedEvent.Payload> {

    public record Payload(
            Long stockTransferId,
            Long organizationId,
            Long shipmentId,
            ZonedDateTime dispatchedAt
    ) {}

    public static StockTransferDispatchedEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new StockTransferDispatchedEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
