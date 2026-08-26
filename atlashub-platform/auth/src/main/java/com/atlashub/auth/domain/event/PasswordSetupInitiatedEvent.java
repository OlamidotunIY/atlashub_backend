package com.atlashub.auth.domain.event;

import com.atlashub.shared.event.DomainEvent;
import java.time.ZonedDateTime;

public record PasswordSetupInitiatedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        Payload payload
) implements DomainEvent<PasswordSetupInitiatedEvent.Payload> {
    public record Payload(String identifier, String setupToken) {}
}