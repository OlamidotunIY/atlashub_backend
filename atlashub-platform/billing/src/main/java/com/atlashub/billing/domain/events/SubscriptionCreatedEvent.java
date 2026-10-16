package com.atlashub.billing.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SubscriptionCreatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<SubscriptionCreatedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long subscriptionId,
            Long organizationId,
            Long pricingPlanId,
            String billingCurrency,
            LocalDate trialEndsAt,
            ZonedDateTime createdAt
    ) {
    }

    public static SubscriptionCreatedEvent of(
            Long subscriptionId,
            Long organizationId,
            Long pricingPlanId,
            String billingCurrency,
            LocalDate trialEndsAt
    ) {
        return new SubscriptionCreatedEvent(
                UUID.randomUUID().toString(),
                subscriptionId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(
                        subscriptionId,
                        organizationId,
                        pricingPlanId,
                        billingCurrency,
                        trialEndsAt,
                        ZonedDateTime.now()
                )
        );
    }
}
