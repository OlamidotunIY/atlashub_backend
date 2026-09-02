package com.atlashub.billing.domain.events;

import com.atlashub.shared.event.DomainEvent;

import java.time.ZonedDateTime;

public record ProductSubscribedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        ProductSubscribedEvent.Payload payload
) implements DomainEvent<ProductSubscribedEvent.Payload> {
    public record Payload(Long organizationId, Long productId) {}
}
