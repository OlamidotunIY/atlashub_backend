package com.atlashub.catalog.domain.events;

import com.atlashub.catalog.domain.valueobject.BillingCycle;
import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.ZonedDateTime;

public record ProductPricingUpdatedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<ProductPricingUpdatedEvent.Payload> {
    public record Payload(Long hubProductId, BillingCycle billingCycle, Money amount) {}
}
