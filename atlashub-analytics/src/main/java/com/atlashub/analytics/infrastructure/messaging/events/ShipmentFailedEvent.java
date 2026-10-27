package com.atlashub.analytics.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ShipmentFailedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<ShipmentFailedEvent.Payload> {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long shipmentId,
            Long organizationId,
            Long orderId,
            String trackingNumber,
            String faultType,
            String reason,
            int deliveryAttempts,
            ZonedDateTime failedAt
    ) {
    }
}
