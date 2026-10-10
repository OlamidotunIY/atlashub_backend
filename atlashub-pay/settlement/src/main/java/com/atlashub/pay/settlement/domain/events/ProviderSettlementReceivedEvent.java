package com.atlashub.pay.settlement.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.ZonedDateTime;

public record ProviderSettlementReceivedEvent(
        String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload)
        implements DomainEvent<ProviderSettlementReceivedEvent.Payload> {

    public record Payload(Long organizationId, String environment, String provider, String settlementReference,
                          Money amount, ZonedDateTime settledAt) {
    }
}
