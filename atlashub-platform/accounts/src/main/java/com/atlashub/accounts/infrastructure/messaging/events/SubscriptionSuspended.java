package com.atlashub.accounts.infrastructure.messaging.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

/**
 * Inbox event: received from billing-events when a subscription is suspended.
 * Accounts reacts by marking the organization's subscription status accordingly.
 */
public record SubscriptionSuspended(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<SubscriptionSuspended.Payload> {
    public record Payload(
            Long organizationId,
            String reason
    ) {}
}
