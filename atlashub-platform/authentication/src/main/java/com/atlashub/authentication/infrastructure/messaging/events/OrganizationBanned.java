package com.atlashub.authentication.infrastructure.messaging.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record OrganizationBanned(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<OrganizationBanned.Payload> {
    public record Payload(Long organizationId, String reason, ZonedDateTime bannedAt) {
    }
}
