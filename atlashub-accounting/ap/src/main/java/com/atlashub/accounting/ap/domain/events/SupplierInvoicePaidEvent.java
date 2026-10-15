package com.atlashub.accounting.ap.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SupplierInvoicePaidEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<SupplierInvoicePaidEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long invoiceId,
            Long organizationId,
            Long supplierId,
            String payoutReference,
            ZonedDateTime paidAt
    ) {}

    public static SupplierInvoicePaidEvent of(
            Long invoiceId,
            Long organizationId,
            Long supplierId,
            String payoutReference,
            ZonedDateTime paidAt
    ) {
        ZonedDateTime now = ZonedDateTime.now();
        return new SupplierInvoicePaidEvent(
                UUID.randomUUID().toString(),
                invoiceId,
                now,
                UUID.randomUUID().toString(),
                new Payload(
                        invoiceId,
                        organizationId,
                        supplierId,
                        payoutReference,
                        paidAt != null ? paidAt : now
                )
        );
    }
}
