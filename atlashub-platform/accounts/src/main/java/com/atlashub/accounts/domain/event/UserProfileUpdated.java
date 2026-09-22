package com.atlashub.accounts.domain.event;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record UserProfileUpdated(
    String eventId,
    Long aggregateId,
    ZonedDateTime occurredAt,
    String correlationId,
    Payload payload
) implements DomainEvent<UserProfileUpdated.Payload> {
    public record Payload(
        String firstName,
        String lastName,
        String phone
    ) {}
}
