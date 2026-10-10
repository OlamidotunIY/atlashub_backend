package com.atlashub.commerce.storefront.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SalesOrderPaymentInitiatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<SalesOrderPaymentInitiatedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long salesOrderId,
            Long organizationId,
            Long outletId,
            String chargeReference,
            BigDecimal amount,
            String currency,
            String paymentMethod,
            Long cashierId,
            ZonedDateTime initiatedAt
    ) {
    }

    public static SalesOrderPaymentInitiatedEvent of(
            Long salesOrderId,
            Long organizationId,
            Long outletId,
            String chargeReference,
            BigDecimal amount,
            String currency,
            String paymentMethod,
            Long cashierId
    ) {
        ZonedDateTime now = ZonedDateTime.now();
        return new SalesOrderPaymentInitiatedEvent(
                UUID.randomUUID().toString(),
                salesOrderId,
                now,
                UUID.randomUUID().toString(),
                new Payload(
                        salesOrderId,
                        organizationId,
                        outletId,
                        chargeReference,
                        amount,
                        currency,
                        paymentMethod,
                        cashierId,
                        now
                )
        );
    }
}
