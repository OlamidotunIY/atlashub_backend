package com.atlashub.hr.payroll.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PayrollApprovedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<PayrollApprovedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long payrollRunId,
            Long organizationId,
            String period,
            BigDecimal totalAmount,
            String currency,
            Long approvedByUserId,
            ZonedDateTime approvedAt
    ) {
    }

    public static PayrollApprovedEvent of(
            Long payrollRunId,
            Long organizationId,
            String period,
            BigDecimal totalAmount,
            String currency,
            Long approvedByUserId
    ) {
        return new PayrollApprovedEvent(
                UUID.randomUUID().toString(),
                payrollRunId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(
                        payrollRunId,
                        organizationId,
                        period,
                        totalAmount,
                        currency,
                        approvedByUserId,
                        ZonedDateTime.now()
                )
        );
    }
}
