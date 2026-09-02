package com.atlashub.catalog.domain.events;

import com.atlashub.catalog.domain.valueobject.ProductKey;
import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record HubProductCreatedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        Payload payload
) implements DomainEvent<HubProductCreatedEvent.Payload> {
    public record Payload(ProductKey key) {
    }
}
