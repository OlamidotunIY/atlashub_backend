package com.atlashub.billing.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SubscriptionRestoredEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<SubscriptionRestoredEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long subscriptionId,
            Long organizationId,
            ZonedDateTime restoredAt
    ) {
    }

    public static SubscriptionRestoredEvent of(
            Long subscriptionId,
            Long organizationId
    ) {
        return new SubscriptionRestoredEvent(
                UUID.randomUUID().toString(),
                subscriptionId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(
                        subscriptionId,
                        organizationId,
                        ZonedDateTime.now()
                )
        );
    }
}
