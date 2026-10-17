package com.atlashub.logistics.shipping.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;
import java.util.UUID;

public record ShipmentDispatchedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<ShipmentDispatchedEvent.Payload> {

    public record Payload(
            Long shipmentId,
            Long organizationId,
            String trackingNumber,
            String fulfillmentChannel,
            String originCity,
            String destinationCity,
            ZonedDateTime dispatchedAt
    ) {}

    public static ShipmentDispatchedEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new ShipmentDispatchedEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
