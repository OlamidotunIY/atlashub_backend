package com.atlashub.identity.domain.event;

import com.atlashub.identity.domain.valueobject.ApiEnvironment;
import com.atlashub.identity.domain.valueobject.KeyType;
import com.atlashub.shared.event.DomainEvent;

import java.time.ZonedDateTime;

public record ApiKeyGenerated(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<ApiKeyGenerated.Payload> {
    public record Payload(
        String OrganizationId,
        KeyType keyType,
        ApiEnvironment environment,
        String prefix
    ) {}
}

