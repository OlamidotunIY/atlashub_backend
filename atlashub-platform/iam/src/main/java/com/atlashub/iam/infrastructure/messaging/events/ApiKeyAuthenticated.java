package com.atlashub.iam.infrastructure.messaging.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record ApiKeyAuthenticated(
        String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload
) implements DomainEvent<ApiKeyAuthenticated.Payload> {
    public record Payload(Long organizationId, String publicKey, ZonedDateTime authenticatedAt) {}
}
