package com.atlashub.billing.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InvoiceIssuedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<InvoiceIssuedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long invoiceId,
            Long organizationId,
            Long subscriptionId,
            String invoiceNumber,
            String productCode,
            BigDecimal totalAmount,
            String currency,
            LocalDate dueDate,
            ZonedDateTime issuedAt
    ) {
    }

    public static InvoiceIssuedEvent of(
            Long invoiceId,
            Long organizationId,
            Long subscriptionId,
            String invoiceNumber,
            String productCode,
            BigDecimal totalAmount,
            String currency,
            LocalDate dueDate
    ) {
        return new InvoiceIssuedEvent(
                UUID.randomUUID().toString(),
                invoiceId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(
                        invoiceId,
                        organizationId,
                        subscriptionId,
                        invoiceNumber,
                        productCode,
                        totalAmount,
                        currency,
                        dueDate,
                        ZonedDateTime.now()
                )
        );
    }
}
