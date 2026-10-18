package com.atlashub.logistics.warehousing.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;
import java.util.UUID;

public record StockTransferReceivedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<StockTransferReceivedEvent.Payload> {

    public record Payload(
            Long stockTransferId,
            Long organizationId,
            Long destinationId,
            ZonedDateTime receivedAt
    ) {}

    public static StockTransferReceivedEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new StockTransferReceivedEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
