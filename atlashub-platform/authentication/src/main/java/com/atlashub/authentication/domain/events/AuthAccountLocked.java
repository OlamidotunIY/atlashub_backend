package com.atlashub.authentication.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record AuthAccountLocked(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<AuthAccountLocked.Payload> {
    public record Payload(
            ZonedDateTime lockedUntil
    ) {
    }
}
