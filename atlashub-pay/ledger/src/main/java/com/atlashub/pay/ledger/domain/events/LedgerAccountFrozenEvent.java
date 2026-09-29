package com.atlashub.pay.ledger.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record LedgerAccountFrozenEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<LedgerAccountFrozenEvent.Payload> {

    public record Payload(
        Long accountId
    ) {
    }
}
