package com.atlashub.billing.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record SubscriptionRenewedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        SubscriptionRenewedEvent.Payload payload
) implements DomainEvent<SubscriptionRenewedEvent.Payload> {
    public record Payload(ZonedDateTime newEnd) {
    }
}
