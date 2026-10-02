package com.atlashub.compliance.infrastructure.messaging.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record OrganizationRegistered(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<OrganizationRegistered.Payload> {
    public record Payload(
            String businessName,
            String registrationType,
            String industry,
            String country,
            String currency,
            Long ownerUserId
    ) {
    }
}

