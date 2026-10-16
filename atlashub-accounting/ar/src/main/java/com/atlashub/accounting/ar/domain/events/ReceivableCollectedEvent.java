package com.atlashub.accounting.ar.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReceivableCollectedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<ReceivableCollectedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long receivableId,
            Long organizationId,
            Long customerId,
            Long salesOrderId,
            BigDecimal amountPaid,
            BigDecimal remainingBalance,
            String currency,
            ZonedDateTime collectedAt
    ) {}

    public static ReceivableCollectedEvent of(
            Long receivableId,
            Long organizationId,
            Long customerId,
            Long salesOrderId,
            BigDecimal amountPaid,
            BigDecimal remainingBalance,
            String currency,
            ZonedDateTime collectedAt
    ) {
        ZonedDateTime now = ZonedDateTime.now();
        return new ReceivableCollectedEvent(
                UUID.randomUUID().toString(),
                receivableId,
                now,
                UUID.randomUUID().toString(),
                new Payload(
                        receivableId,
                        organizationId,
                        customerId,
                        salesOrderId,
                        amountPaid,
                        remainingBalance,
                        currency,
                        collectedAt != null ? collectedAt : now
                )
        );
    }
}
