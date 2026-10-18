package com.atlashub.commerce.storefront.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;
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

    public record ItemPayload(Long productId, Long variantId, int quantity, BigDecimal totalAmount) {}

    public static PosSaleRefundedEvent of(
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
            List<ItemPayload> items
    ) {
        ZonedDateTime now = ZonedDateTime.now();
        return new PosSaleRefundedEvent(
                UUID.randomUUID().toString(),
                salesOrderId,
                now,
                UUID.randomUUID().toString(),
                new Payload(
                        salesOrderId,
                        organizationId,
                        outletId,
                        customerId,
                        chargeReference,
                        totalGross,
                        totalDiscount,
                        totalTax,
                        refundAmount,
                        currency,
                        paymentMethod,
                        refundReason,
                        customerNuban,
                        customerBankCode,
                        items != null ? List.copyOf(items) : List.of(),
                        now
                )
        );
    }
}
