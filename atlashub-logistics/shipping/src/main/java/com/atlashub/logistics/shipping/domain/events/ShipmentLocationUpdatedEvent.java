package com.atlashub.logistics.shipping.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public record ShipmentLocationUpdatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<ShipmentLocationUpdatedEvent.Payload> {

    public record Payload(
            Long shipmentId,
            Long organizationId,
            String trackingNumber,
            String locationName,
            BigDecimal latitude,
            BigDecimal longitude,
            ZonedDateTime timestamp
    ) {}

    public static ShipmentLocationUpdatedEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new ShipmentLocationUpdatedEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
