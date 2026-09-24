package com.atlashub.accounts.domain.event;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record UserCreated(
    String eventId,
    Long aggregateId,
    ZonedDateTime occurredAt,
    String correlationId,
    Payload payload
) implements DomainEvent<UserCreated.Payload> {
    public record Payload(
        String email,
        Boolean isInvited,
        String hashedPassword
    ) {}
}
