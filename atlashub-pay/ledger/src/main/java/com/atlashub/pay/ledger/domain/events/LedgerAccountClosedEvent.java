package com.atlashub.pay.ledger.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record LedgerAccountClosedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<LedgerAccountClosedEvent.Payload> {

    public record Payload(
        Long accountId
    ) {
    }
}
