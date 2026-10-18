package com.atlashub.billing.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InvoiceVoidedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<InvoiceVoidedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long invoiceId,
            Long organizationId,
            Long subscriptionId,
            String invoiceNumber,
            String reason,
            ZonedDateTime voidedAt
    ) {
    }

    public static InvoiceVoidedEvent of(
            Long invoiceId,
            Long organizationId,
            Long subscriptionId,
            String invoiceNumber,
            String reason
    ) {
        return new InvoiceVoidedEvent(
                UUID.randomUUID().toString(),
                invoiceId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(
                        invoiceId,
                        organizationId,
                        subscriptionId,
                        invoiceNumber,
                        reason,
                        ZonedDateTime.now()
                )
        );
    }
}
