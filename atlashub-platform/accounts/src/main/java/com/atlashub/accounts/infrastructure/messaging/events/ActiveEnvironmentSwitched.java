package com.atlashub.accounts.infrastructure.messaging.events;

import com.atlashub.shared.application.security.ApiEnvironment;

import java.time.ZonedDateTime;

/** Consumer-owned copy of Authentication's active-environment event. */
public record ActiveEnvironmentSwitched(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) {
    public record Payload(Long userId, ApiEnvironment environment) {}
}
