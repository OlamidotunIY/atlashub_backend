package com.atlashub.commerce.storefront.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

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
            String chargeReference,
            BigDecimal refundAmount,
            String currency,
            String refundReason,
            String customerNuban,
            String customerBankCode,
            ZonedDateTime refundedAt
    ) {
    }

    public static PosSaleRefundedEvent of(
            Long salesOrderId,
            Long organizationId,
            String chargeReference,
            BigDecimal refundAmount,
            String currency,
            String refundReason,
            String customerNuban,
            String customerBankCode
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
                        chargeReference,
                        refundAmount,
                        currency,
                        refundReason,
                        customerNuban,
                        customerBankCode,
                        now
                )
        );
    }
}
