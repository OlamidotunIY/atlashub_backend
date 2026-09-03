package com.atlashub.billing.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record WalletChargeSuccessfulEvent(
        String eventId,
        String aggregateId, // This is the invoiceId we sent in WalletChargeRequestedEvent
        ZonedDateTime occurredAt,
        Payload payload
) implements DomainEvent<WalletChargeSuccessfulEvent.Payload> {
    public record Payload(
            Long invoiceId,
            String transactionReference
    ) {}
}