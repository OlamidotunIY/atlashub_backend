package com.atlashub.logistics.shipping.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;
import java.util.UUID;

public record ShipmentAssignedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<ShipmentAssignedEvent.Payload> {

    public record Payload(
            Long shipmentId,
            Long organizationId,
            String trackingNumber,
            String fulfillmentChannel,
            Long riderId,
            Long vehicleId,
            String carrier3PLCode,
            String carrier3PLReference,
            ZonedDateTime assignedAt
    ) {}

    public static ShipmentAssignedEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new ShipmentAssignedEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
