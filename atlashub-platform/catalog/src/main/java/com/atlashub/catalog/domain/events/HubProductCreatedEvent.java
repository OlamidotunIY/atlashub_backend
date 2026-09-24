package com.atlashub.catalog.domain.events;

import com.atlashub.catalog.domain.valueobject.ProductKey;
import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record HubProductCreatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<HubProductCreatedEvent.Payload> {
    public record Payload(Long id, ProductKey key, String name, String description, com.atlashub.catalog.domain.valueobject.ProductStatus status) {
    }
}
