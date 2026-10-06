package com.atlashub.iam.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record ApiKeyAuthenticatedEvent(
        String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload
) implements DomainEvent<ApiKeyAuthenticatedEvent.Payload> {
    public record Payload(Long organizationId, String publicKey, ZonedDateTime authenticatedAt) {}
}
