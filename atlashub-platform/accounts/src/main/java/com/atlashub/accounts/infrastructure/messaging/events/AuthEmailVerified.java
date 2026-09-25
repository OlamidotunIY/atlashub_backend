package com.atlashub.accounts.infrastructure.messaging.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

/**
 * Mirror of the authentication module's AuthEmailVerifiedEvent.
 * Used to deserialize the Kafka message in the accounts module.
 */
public record AuthEmailVerified(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<AuthEmailVerified.Payload> {

    public record Payload(
            String userId,
            String email
    ) {
    }
}
