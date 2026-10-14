package com.atlashub.accounting.cash.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public final class CashInboundEvents {

    private CashInboundEvents() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TillClosedEvent(
            String eventId,
            Long aggregateId,
            ZonedDateTime occurredAt,
            String correlationId,
            Payload payload
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Payload(
                Long tillSessionId,
                Long organizationId,
                Long outletId,
                Long cashierId,
                BigDecimal closingCash,
                BigDecimal totalSales,
                String currency,
                ZonedDateTime closedAt
        ) {
        }
    }
}
