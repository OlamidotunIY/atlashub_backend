package com.atlashub.billing.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SubscriptionRenewedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<SubscriptionRenewedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long subscriptionId,
            Long organizationId,
            LocalDate currentPeriodStart,
            LocalDate currentPeriodEnd,
            ZonedDateTime renewedAt
    ) {
    }

    public static SubscriptionRenewedEvent of(
            Long subscriptionId,
            Long organizationId,
            LocalDate currentPeriodStart,
            LocalDate currentPeriodEnd
    ) {
        return new SubscriptionRenewedEvent(
                UUID.randomUUID().toString(),
                subscriptionId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(
                        subscriptionId,
                        organizationId,
                        currentPeriodStart,
                        currentPeriodEnd,
                        ZonedDateTime.now()
                )
        );
    }
}
