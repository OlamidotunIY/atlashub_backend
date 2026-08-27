package com.atlashub.identity.domain.event;

import com.atlashub.shared.event.DomainEvent;
import java.time.ZonedDateTime;

public record UserCreated(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<UserCreated.Payload> {
    public record Payload(
        String email,
        String firstName,
        String lastName,
        String country
    ) {}
}
