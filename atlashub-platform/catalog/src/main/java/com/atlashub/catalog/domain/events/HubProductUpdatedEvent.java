package com.atlashub.catalog.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record HubProductUpdatedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<HubProductUpdatedEvent.Payload> {
    public record Payload(String name, String description) {}
}
