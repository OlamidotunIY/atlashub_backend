package com.atlashub.auth.domain.event;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record AuthPasswordSetupInitiatedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        Payload payload
) implements DomainEvent<AuthPasswordSetupInitiatedEvent.Payload> {
    public record Payload(String identifier, String setupToken) {}
}
