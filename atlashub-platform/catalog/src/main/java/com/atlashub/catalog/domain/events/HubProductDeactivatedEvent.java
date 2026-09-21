package com.atlashub.catalog.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record HubProductDeactivatedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Void payload
) implements DomainEvent<Void> {
}
