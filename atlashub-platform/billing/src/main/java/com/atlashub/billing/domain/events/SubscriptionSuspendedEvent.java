package com.atlashub.billing.domain.events;

import com.atlashub.shared.event.DomainEvent;

import java.time.ZonedDateTime;

public record SubscriptionSuspendedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        SubscriptionSuspendedEvent.Payload payload
) implements DomainEvent<SubscriptionSuspendedEvent.Payload> {
    public record Payload(String reason) {
    }
}
