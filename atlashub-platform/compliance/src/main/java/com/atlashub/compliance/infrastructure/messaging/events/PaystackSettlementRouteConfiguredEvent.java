package com.atlashub.compliance.infrastructure.messaging.events;

import java.time.ZonedDateTime;
import java.util.Set;

public record PaystackSettlementRouteConfiguredEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) {
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
