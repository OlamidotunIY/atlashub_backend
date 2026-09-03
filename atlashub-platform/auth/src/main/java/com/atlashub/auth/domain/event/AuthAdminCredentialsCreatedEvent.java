package com.atlashub.auth.domain.event;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record AuthAdminCredentialsCreatedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        Payload payload
) implements DomainEvent<AuthAdminCredentialsCreatedEvent.Payload> {
    public record Payload(String personalEmail, String temporaryPassword) {}
}