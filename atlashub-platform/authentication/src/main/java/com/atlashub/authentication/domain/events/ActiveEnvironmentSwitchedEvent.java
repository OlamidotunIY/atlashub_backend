package com.atlashub.authentication.domain.events;

import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record ActiveEnvironmentSwitchedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<ActiveEnvironmentSwitchedEvent.Payload> {
    public record Payload(Long userId, ApiEnvironment environment) {}
}
