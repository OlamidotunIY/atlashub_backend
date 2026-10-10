package com.atlashub.commerce.catalog.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record VendorTerminatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<VendorTerminatedEvent.Payload> {

    public record Payload(
            Long organizationId,
            Long userId,
            String businessName,
            ZonedDateTime terminatedAt
    ) {
    }
}
