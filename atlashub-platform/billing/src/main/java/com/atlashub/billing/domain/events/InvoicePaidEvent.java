package com.atlashub.billing.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InvoicePaidEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<InvoicePaidEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long invoiceId,
            Long organizationId,
            Long subscriptionId,
            String invoiceNumber,
            String paymentReference,
            ZonedDateTime paidAt
    ) {
    }

    public static InvoicePaidEvent of(
            Long invoiceId,
            Long organizationId,
            Long subscriptionId,
            String invoiceNumber,
            String paymentReference,
            ZonedDateTime paidAt
    ) {
        return new InvoicePaidEvent(
                UUID.randomUUID().toString(),
                invoiceId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(
                        invoiceId,
                        organizationId,
                        subscriptionId,
                        invoiceNumber,
                        paymentReference,
                        paidAt != null ? paidAt : ZonedDateTime.now()
                )
        );
    }
}
