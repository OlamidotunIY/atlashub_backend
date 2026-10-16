package com.atlashub.hr.payroll.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PayrollPendingApprovalEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<PayrollPendingApprovalEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long payrollRunId,
            Long organizationId,
            String period,
            Long initiatorUserId
    ) {
    }

    public static PayrollPendingApprovalEvent of(
            Long payrollRunId,
            Long organizationId,
            String period,
            Long initiatorUserId
    ) {
        return new PayrollPendingApprovalEvent(
                UUID.randomUUID().toString(),
                payrollRunId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(payrollRunId, organizationId, period, initiatorUserId)
        );
    }
}
