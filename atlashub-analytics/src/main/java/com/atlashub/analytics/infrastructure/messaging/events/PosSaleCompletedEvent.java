package com.atlashub.analytics.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.atlashub.shared.domain.event.DomainEvent;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PosSaleCompletedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<PosSaleCompletedEvent.Payload> {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long salesOrderId,
            Long organizationId,
            Long outletId,
            Long customerId,
            BigDecimal totalGross,
            BigDecimal totalDiscount,
            BigDecimal totalTax,
            BigDecimal totalNet,
            String currency,
            String paymentMethod,
            Long cashierId,
            List<ItemPayload> items,
            ZonedDateTime completedAt
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItemPayload(Long productId, Long variantId, int quantity, BigDecimal totalAmount) {}
}
