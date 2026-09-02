package com.atlashub.billing.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record BillingInvoicePaidEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        BillingInvoicePaidEvent.Payload payload
) implements DomainEvent<BillingInvoicePaidEvent.Payload> {
    public record Payload(Long invoiceId) {
    }
}
