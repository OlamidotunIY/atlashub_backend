package com.atlashub.compliance.infrastructure.messaging.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record KycRejectedEvent(
        String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload
) implements DomainEvent<KycRejectedEvent.Payload> {
    public record Payload(Long organizationId, String anchorBusinessCustomerId, String reason) {}
}
