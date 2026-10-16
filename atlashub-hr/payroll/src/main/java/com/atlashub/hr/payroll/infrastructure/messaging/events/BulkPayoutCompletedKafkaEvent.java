package com.atlashub.hr.payroll.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BulkPayoutCompletedKafkaEvent(
        String eventId,
        String aggregateId,
        Payload payload
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long payrollRunId,
            Long organizationId,
            BigDecimal totalAmountDisbursed,
            ZonedDateTime disbursedAt
    ) {
    }
}
