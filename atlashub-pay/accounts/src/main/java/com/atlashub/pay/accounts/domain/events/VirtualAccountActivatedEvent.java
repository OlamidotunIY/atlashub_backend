package com.atlashub.pay.accounts.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.valueobject.CurrencyCode;

import java.time.ZonedDateTime;

public record VirtualAccountActivatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<VirtualAccountActivatedEvent.Payload> {

    public record Payload(
            Long virtualAccountId,
            Long organizationId,
            String ownerType,
            String customerId,
            String nuban,
            String bankName,
            String bankCode,
            CurrencyCode currency,
            String accountName
    ) {
    }
}
