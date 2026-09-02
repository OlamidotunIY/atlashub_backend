package com.atlashub.billing.domain.events;

import com.atlashub.shared.event.DomainEvent;

import java.time.ZonedDateTime;

public record SubscriptionCanceledEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        SubscriptionCanceledEvent.Payload payload
) implements DomainEvent<SubscriptionCanceledEvent.Payload> {
    public record Payload(String reason) {
    }
}
