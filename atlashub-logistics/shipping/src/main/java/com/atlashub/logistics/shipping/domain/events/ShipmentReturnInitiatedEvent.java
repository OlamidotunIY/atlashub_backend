package com.atlashub.logistics.shipping.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;
import java.util.UUID;

public record ShipmentReturnInitiatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<ShipmentReturnInitiatedEvent.Payload> {

    public record Payload(
            Long shipmentId,
            Long organizationId,
            String trackingNumber,
            String reason,
            ZonedDateTime returnInitiatedAt
    ) {}

    public static ShipmentReturnInitiatedEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new ShipmentReturnInitiatedEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
