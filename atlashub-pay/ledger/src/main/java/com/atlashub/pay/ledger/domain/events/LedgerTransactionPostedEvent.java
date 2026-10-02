package com.atlashub.pay.ledger.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;
import java.math.BigDecimal;
import java.util.List;

public record LedgerTransactionPostedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<LedgerTransactionPostedEvent.Payload> {

    public record Payload(
        Long transactionId,
        Long organizationId,
        String reference,
        String sourceSystem,
        String sourceReferenceId,
        String description,
        String currency,
        ZonedDateTime postedAt,
        List<EntryPayload> entries
    ) {
    }

    public record EntryPayload(
        Long accountId,
        String entryType,
        BigDecimal amount
    ) {}
}
