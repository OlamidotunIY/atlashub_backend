package com.atlashub.accounts.infrastructure.messaging.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record ActiveOrganizationSwitched(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<ActiveOrganizationSwitched.Payload> {
    public record Payload(Long userId, Long organizationId) {}
}
