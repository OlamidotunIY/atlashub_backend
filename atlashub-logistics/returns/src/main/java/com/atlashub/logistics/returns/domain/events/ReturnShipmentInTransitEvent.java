package com.atlashub.logistics.returns.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;
import java.util.UUID;

public record ReturnShipmentInTransitEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<ReturnShipmentInTransitEvent.Payload> {

    public record Payload(
            Long returnShipmentId,
            Long organizationId,
            String returnTrackingNumber,
            ZonedDateTime inTransitAt
    ) {}

    public static ReturnShipmentInTransitEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new ReturnShipmentInTransitEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
