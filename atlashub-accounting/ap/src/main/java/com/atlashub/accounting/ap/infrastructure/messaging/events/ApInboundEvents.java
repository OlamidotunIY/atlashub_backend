package com.atlashub.accounting.ap.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

public final class ApInboundEvents {

    private ApInboundEvents() {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PurchaseOrderReceivedEvent(
            String eventId,
            Long aggregateId,
            ZonedDateTime occurredAt,
            String correlationId,
            PurchaseOrderReceivedPayload payload
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PurchaseOrderReceivedPayload(
            Long organizationId,
            Long supplierId,
            Long purchaseOrderId,
            String invoiceNumber,
            BigDecimal amount,
            String currency,
            LocalDate dueDate
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PayoutCompletedEvent(
            String eventId,
            Long aggregateId,
            ZonedDateTime occurredAt,
            String correlationId,
            PayoutCompletedPayload payload
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PayoutCompletedPayload(
            Long invoiceId,
            String payoutReference,
            ZonedDateTime paidAt
    ) {}
}
