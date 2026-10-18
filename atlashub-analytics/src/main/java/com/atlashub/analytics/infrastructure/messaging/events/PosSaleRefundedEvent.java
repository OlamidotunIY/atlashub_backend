package com.atlashub.analytics.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.atlashub.shared.domain.event.DomainEvent;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PosSaleRefundedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<PosSaleRefundedEvent.Payload> {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long salesOrderId,
            Long organizationId,
            Long outletId,
            Long customerId,
            String chargeReference,
            BigDecimal totalGross,
            BigDecimal totalDiscount,
            BigDecimal totalTax,
            BigDecimal refundAmount,
            String currency,
            String paymentMethod,
            String refundReason,
            String customerNuban,
            String customerBankCode,
            List<ItemPayload> items,
            ZonedDateTime refundedAt
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItemPayload(Long productId, Long variantId, int quantity, BigDecimal totalAmount) {}
}
