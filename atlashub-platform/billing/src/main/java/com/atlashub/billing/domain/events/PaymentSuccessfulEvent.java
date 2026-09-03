package com.atlashub.billing.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record PaymentSuccessfulEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        Payload payload
) implements DomainEvent<PaymentSuccessfulEvent.Payload> {
    public record Payload(
            String reference,
            String purpose, // COMMERCE_ORDER, PLATFORM_INVOICE, WALLET_FUNDING
            String metadata // JSON string containing invoiceId
    ) {}
}