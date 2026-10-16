package com.atlashub.hr.payroll.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BulkPayoutFailedKafkaEvent(
        String eventId,
        String aggregateId,
        Payload payload
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long payrollRunId,
            Long organizationId,
            String failureReason
    ) {
    }
}
