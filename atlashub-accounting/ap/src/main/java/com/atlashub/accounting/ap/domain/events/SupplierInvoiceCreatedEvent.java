package com.atlashub.accounting.ap.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SupplierInvoiceCreatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<SupplierInvoiceCreatedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long invoiceId,
            Long organizationId,
            Long supplierId,
            Long purchaseOrderId,
            String invoiceNumber,
            BigDecimal amount,
            String currency,
            LocalDate dueDate
    ) {}

    public static SupplierInvoiceCreatedEvent of(
            Long invoiceId,
            Long organizationId,
            Long supplierId,
            Long purchaseOrderId,
            String invoiceNumber,
            BigDecimal amount,
            String currency,
            LocalDate dueDate
    ) {
        return new SupplierInvoiceCreatedEvent(
                UUID.randomUUID().toString(),
                invoiceId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(
                        invoiceId,
                        organizationId,
                        supplierId,
                        purchaseOrderId,
                        invoiceNumber,
                        amount,
                        currency,
                        dueDate
                )
        );
    }
}
