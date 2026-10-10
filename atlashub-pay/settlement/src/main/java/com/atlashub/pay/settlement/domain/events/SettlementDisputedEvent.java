package com.atlashub.pay.settlement.domain.events;

import com.atlashub.pay.settlement.domain.valueobject.PaymentProvider;
import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.ZonedDateTime;

public record SettlementDisputedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<SettlementDisputedEvent.Payload> {

    public record Payload(
            Long organizationId,
            PaymentProvider provider,
            String providerSettlementId,
            Money netAmount,
            String reason,
            ZonedDateTime disputedAt
    ) {
    }
}
