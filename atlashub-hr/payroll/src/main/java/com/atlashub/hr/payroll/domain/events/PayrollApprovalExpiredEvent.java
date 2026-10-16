package com.atlashub.hr.payroll.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PayrollApprovalExpiredEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<PayrollApprovalExpiredEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long payrollRunId,
            Long organizationId,
            String period,
            Long initiatorUserId
    ) {
    }

    public static PayrollApprovalExpiredEvent of(
            Long payrollRunId,
            Long organizationId,
            String period,
            Long initiatorUserId
    ) {
        return new PayrollApprovalExpiredEvent(
                UUID.randomUUID().toString(),
                payrollRunId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(payrollRunId, organizationId, period, initiatorUserId)
        );
    }
}
