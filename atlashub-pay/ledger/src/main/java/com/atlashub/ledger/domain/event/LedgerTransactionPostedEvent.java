package com.atlashub.ledger.domain.event;

import com.atlashub.shared.domain.event.DomainEvent;

import com.atlashub.ledger.domain.valueobject.SourceSystem;

public record LedgerTransactionPostedEvent(
    String eventId,
    String aggregateId,
    java.time.ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<LedgerTransactionPostedEvent.Payload> {
    public record Payload(
        String transactionReference,
        SourceSystem sourceSystem
    ) {}
}
