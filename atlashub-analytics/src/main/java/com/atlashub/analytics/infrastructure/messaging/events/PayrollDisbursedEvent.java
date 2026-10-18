package com.atlashub.analytics.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.atlashub.shared.domain.event.DomainEvent;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PayrollDisbursedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<PayrollDisbursedEvent.Payload> {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long payrollRunId,
            Long organizationId,
            String period,
            BigDecimal totalAmount,
            BigDecimal totalGross,
            BigDecimal totalDeductions,
            BigDecimal totalNet,
            int headcount,
            String currency,
            ZonedDateTime disbursedAt
    ) {
    }
}
