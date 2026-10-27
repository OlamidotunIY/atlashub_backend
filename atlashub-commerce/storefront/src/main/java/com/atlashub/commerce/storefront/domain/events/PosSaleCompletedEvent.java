package com.atlashub.commerce.storefront.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;
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

    public record ItemPayload(Long productId, Long variantId, int quantity, BigDecimal totalAmount) {}

    public static PosSaleCompletedEvent of(
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
            List<ItemPayload> items
    ) {
        ZonedDateTime now = ZonedDateTime.now();
        return new PosSaleCompletedEvent(
                UUID.randomUUID().toString(),
                salesOrderId,
                now,
                UUID.randomUUID().toString(),
                new Payload(
                        salesOrderId,
                        organizationId,
                        outletId,
                        customerId,
                        totalGross,
                        totalDiscount,
                        totalTax,
                        totalNet,
                        currency,
                        paymentMethod,
                        cashierId,
                        items != null ? List.copyOf(items) : List.of(),
                        now
                )
        );
    }
}
