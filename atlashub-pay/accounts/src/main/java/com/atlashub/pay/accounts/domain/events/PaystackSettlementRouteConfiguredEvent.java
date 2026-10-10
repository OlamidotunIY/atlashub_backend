package com.atlashub.pay.accounts.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;
import java.util.Set;

public record PaystackSettlementRouteConfiguredEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<PaystackSettlementRouteConfiguredEvent.Payload> {
    public record Payload(
            Long organizationId,
            String environment,
            String provider,
            Set<String> capabilities,
            String externalMerchantId,
            String externalAccountId,
            String settlementAccountReference,
            ZonedDateTime configuredAt
    ) {
    }
}
