package com.atlashub.billing.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.money.Money;

import java.time.ZonedDateTime;

public record BillingInvoiceGeneratedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        BillingInvoiceGeneratedEvent.Payload payload
) implements DomainEvent<BillingInvoiceGeneratedEvent.Payload> {
    public record Payload(Long invoiceId, Money amount) {
    }
}
