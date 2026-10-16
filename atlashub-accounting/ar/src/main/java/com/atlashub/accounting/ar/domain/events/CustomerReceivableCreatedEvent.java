package com.atlashub.accounting.ar.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CustomerReceivableCreatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<CustomerReceivableCreatedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long receivableId,
            Long organizationId,
            Long customerId,
            Long salesOrderId,
            BigDecimal amount,
            String currency,
            LocalDate dueDate
    ) {}

    public static CustomerReceivableCreatedEvent of(
            Long receivableId,
            Long organizationId,
            Long customerId,
            Long salesOrderId,
            BigDecimal amount,
            String currency,
            LocalDate dueDate
    ) {
        return new CustomerReceivableCreatedEvent(
                UUID.randomUUID().toString(),
                receivableId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(
                        receivableId,
                        organizationId,
                        customerId,
                        salesOrderId,
                        amount,
                        currency,
                        dueDate
                )
        );
    }
}
