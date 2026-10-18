package com.atlashub.logistics.shipping.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public record ShipmentMilestoneUpdatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<ShipmentMilestoneUpdatedEvent.Payload> {

    public record Payload(
            Long shipmentId,
            Long organizationId,
            String trackingNumber,
            String routeType,
            String milestone,
            String status,
            String locationName,
            String state,
            String description,
            BigDecimal latitude,
            BigDecimal longitude,
            ZonedDateTime timestamp
    ) {}

    public static ShipmentMilestoneUpdatedEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new ShipmentMilestoneUpdatedEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
