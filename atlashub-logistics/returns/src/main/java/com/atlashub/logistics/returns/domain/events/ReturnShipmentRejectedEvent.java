package com.atlashub.logistics.returns.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;
import java.util.UUID;

public record ReturnShipmentRejectedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<ReturnShipmentRejectedEvent.Payload> {

    public record Payload(
            Long returnShipmentId,
            Long organizationId,
            String rejectionReason,
            ZonedDateTime rejectedAt
    ) {}

    public static ReturnShipmentRejectedEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new ReturnShipmentRejectedEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
