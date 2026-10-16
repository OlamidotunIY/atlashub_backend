package com.atlashub.accounting.ar.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

public final class ArInboundEvents {

    private ArInboundEvents() {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CreditSaleCompletedEvent(
            String eventId,
            Long aggregateId,
            ZonedDateTime occurredAt,
            String correlationId,
            CreditSaleCompletedPayload payload
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CreditSaleCompletedPayload(
            Long organizationId,
            Long customerId,
            Long salesOrderId,
            BigDecimal amount,
            String currency,
            LocalDate dueDate
    ) {}
}
