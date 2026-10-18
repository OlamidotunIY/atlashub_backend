package com.atlashub.hr.payroll.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

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
            String currency,
            ZonedDateTime disbursedAt
    ) {
    }

    public static PayrollDisbursedEvent of(
            Long payrollRunId,
            Long organizationId,
            String period,
            BigDecimal totalAmount,
            String currency,
            ZonedDateTime disbursedAt
    ) {
        return new PayrollDisbursedEvent(
                UUID.randomUUID().toString(),
                payrollRunId,
                disbursedAt != null ? disbursedAt : ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(
                        payrollRunId,
                        organizationId,
                        period,
                        totalAmount,
                        currency,
                        disbursedAt
                )
        );
    }
}
