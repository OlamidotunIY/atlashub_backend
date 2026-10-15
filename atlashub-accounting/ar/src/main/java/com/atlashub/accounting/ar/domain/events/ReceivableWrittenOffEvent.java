package com.atlashub.accounting.ar.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReceivableWrittenOffEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<ReceivableWrittenOffEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long receivableId,
            Long organizationId,
            Long customerId,
            Long salesOrderId,
            BigDecimal uncollectedAmount,
            String currency,
            String reason,
            ZonedDateTime writtenOffAt
    ) {}

    public static ReceivableWrittenOffEvent of(
            Long receivableId,
            Long organizationId,
            Long customerId,
            Long salesOrderId,
            BigDecimal uncollectedAmount,
            String currency,
            String reason,
            ZonedDateTime writtenOffAt
    ) {
        ZonedDateTime now = ZonedDateTime.now();
        return new ReceivableWrittenOffEvent(
                UUID.randomUUID().toString(),
                receivableId,
                now,
                UUID.randomUUID().toString(),
                new Payload(
                        receivableId,
                        organizationId,
                        customerId,
                        salesOrderId,
                        uncollectedAmount,
                        currency,
                        reason,
                        writtenOffAt != null ? writtenOffAt : now
                )
        );
    }
}
