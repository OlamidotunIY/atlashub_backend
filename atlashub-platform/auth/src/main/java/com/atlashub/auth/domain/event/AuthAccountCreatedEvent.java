package com.atlashub.auth.domain.event;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record AuthAccountCreatedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        Void payload
) implements DomainEvent<Void> {
}
