package com.atlashub.identity.domain.event;

import com.atlashub.shared.event.DomainEvent;
import java.time.ZonedDateTime;

public record UserProfileUpdated(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<UserProfileUpdated.Payload> {
    public record Payload(
        String firstName,
        String lastName,
        String phone
    ) {}
}
