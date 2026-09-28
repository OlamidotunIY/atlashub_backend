package com.atlashub.pay.ledger.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record LedgerAccountUnfrozenEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<LedgerAccountUnfrozenEvent.Payload> {

    public record Payload(
        Long accountId
    ) {
    }
}
