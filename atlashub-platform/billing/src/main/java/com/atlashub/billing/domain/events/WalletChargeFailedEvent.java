package com.atlashub.billing.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record WalletChargeFailedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        Payload payload
) implements DomainEvent<WalletChargeFailedEvent.Payload> {
    public record Payload(
            Long invoiceId,
            String reason
    ) {}
}