package com.atlashub.ledger.domain.event;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record WalletChargeSuccessfulEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        Payload payload
) implements DomainEvent<WalletChargeSuccessfulEvent.Payload> {
    public record Payload(
            Long invoiceId,
            Long walletChargeId
    ) {}
}
