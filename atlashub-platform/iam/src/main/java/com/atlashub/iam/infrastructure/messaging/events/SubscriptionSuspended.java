package com.atlashub.iam.infrastructure.messaging.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record SubscriptionSuspended(
        String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload
) implements DomainEvent<SubscriptionSuspended.Payload> {
    public record Payload(Long subscriptionId, Long organizationId, ZonedDateTime suspendedAt) {}
}
