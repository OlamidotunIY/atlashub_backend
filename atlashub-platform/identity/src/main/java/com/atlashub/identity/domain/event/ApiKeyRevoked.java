package com.atlashub.identity.domain.event;

import com.atlashub.identity.domain.model.ApiEnvironment;
import com.atlashub.identity.domain.model.KeyType;
import com.atlashub.shared.event.DomainEvent;

import java.time.ZonedDateTime;

public record ApiKeyRevoked(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<ApiKeyRevoked.Payload> {
    public record Payload(
        String OrganizationId,
        KeyType keyType,
        ApiEnvironment environment
    ) {}
}
