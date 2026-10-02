package com.atlashub.compliance.infrastructure.messaging.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record KycApprovedEvent(
        String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload
) implements DomainEvent<KycApprovedEvent.Payload> {
    public record Payload(Long organizationId, String anchorBusinessCustomerId) {}
}
