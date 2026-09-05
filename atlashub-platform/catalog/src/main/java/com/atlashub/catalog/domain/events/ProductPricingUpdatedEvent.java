package com.atlashub.catalog.domain.events;

import com.atlashub.catalog.domain.valueobject.BillingCycle;
import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.money.Money;

import java.time.ZonedDateTime;

public record ProductPricingUpdatedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        Payload payload
) implements DomainEvent<ProductPricingUpdatedEvent.Payload> {
    public record Payload(Long hubProductId, BillingCycle billingCycle, Money amount) {}
}
