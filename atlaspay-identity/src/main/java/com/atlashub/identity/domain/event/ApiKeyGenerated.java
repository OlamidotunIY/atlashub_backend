package com.atlashub.identity.domain.event;

import com.atlashub.identity.domain.model.ApiEnvironment;
import com.atlashub.identity.domain.model.KeyType;
import com.atlashub.shared.event.DomainEvent;

import java.time.ZonedDateTime;

public record ApiKeyGenerated(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<ApiKeyGenerated.Payload> {
    public record Payload(
        String merchantId,
        KeyType keyType,
        ApiEnvironment environment,
        String prefix
    ) {}
}
