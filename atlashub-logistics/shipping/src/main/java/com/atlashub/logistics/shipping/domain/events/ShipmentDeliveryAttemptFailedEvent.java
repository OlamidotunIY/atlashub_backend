package com.atlashub.logistics.shipping.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;
import java.util.UUID;

public record ShipmentDeliveryAttemptFailedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<ShipmentDeliveryAttemptFailedEvent.Payload> {

    public record Payload(
            Long shipmentId,
            Long organizationId,
            String trackingNumber,
            int attemptNumber,
            String reason,
            ZonedDateTime attemptedAt
    ) {}

    public static ShipmentDeliveryAttemptFailedEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new ShipmentDeliveryAttemptFailedEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
