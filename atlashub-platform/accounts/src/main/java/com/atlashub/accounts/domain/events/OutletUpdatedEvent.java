package com.atlashub.accounts.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record OutletUpdatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<OutletUpdatedEvent.Payload> {
    public record Payload(
            String name,
            String address,
            String city,
            String state
    ) {}
}
