package com.atlashub.billing.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SubscriptionCancelledEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<SubscriptionCancelledEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long subscriptionId,
            Long organizationId,
            String reason,
            ZonedDateTime cancelledAt
    ) {
    }

    public static SubscriptionCancelledEvent of(
            Long subscriptionId,
            Long organizationId,
            String reason
    ) {
        return new SubscriptionCancelledEvent(
                UUID.randomUUID().toString(),
                subscriptionId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(
                        subscriptionId,
                        organizationId,
                        reason,
                        ZonedDateTime.now()
                )
        );
    }
}
