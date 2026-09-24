package com.atlashub.authentication.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

/**
 * Published when a user's email address has been successfully verified.
 * The accounts module User listens to this event and sets emailVerified = true.
 */
public record AuthEmailVerifiedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<AuthEmailVerifiedEvent.Payload> {

    public record Payload(
            String userId,   // user's ID as String — the User in accounts module
            String email     // the verified email address
    ) {
    }
}
