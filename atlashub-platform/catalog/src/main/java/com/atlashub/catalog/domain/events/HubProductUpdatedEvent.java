package com.atlashub.catalog.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record HubProductUpdatedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        Void payload
) implements DomainEvent<Void> {
}
