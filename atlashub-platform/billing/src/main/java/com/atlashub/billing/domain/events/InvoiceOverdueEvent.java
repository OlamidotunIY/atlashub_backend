package com.atlashub.billing.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InvoiceOverdueEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<InvoiceOverdueEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long invoiceId,
            Long organizationId,
            Long subscriptionId,
            String invoiceNumber,
            LocalDate dueDate,
            ZonedDateTime markedOverdueAt
    ) {
    }

    public static InvoiceOverdueEvent of(
            Long invoiceId,
            Long organizationId,
            Long subscriptionId,
            String invoiceNumber,
            LocalDate dueDate
    ) {
        return new InvoiceOverdueEvent(
                UUID.randomUUID().toString(),
                invoiceId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(
                        invoiceId,
                        organizationId,
                        subscriptionId,
                        invoiceNumber,
                        dueDate,
                        ZonedDateTime.now()
                )
        );
    }
}
