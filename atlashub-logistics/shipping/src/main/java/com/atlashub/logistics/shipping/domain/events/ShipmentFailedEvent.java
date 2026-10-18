package com.atlashub.logistics.shipping.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;
import java.util.UUID;

public record ShipmentFailedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<ShipmentFailedEvent.Payload> {

    public record Payload(
            Long shipmentId,
            Long organizationId,
            Long orderId,
            String trackingNumber,
            String faultType,
            String reason,
            int deliveryAttempts,
            ZonedDateTime failedAt
    ) {}

    public static ShipmentFailedEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new ShipmentFailedEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
