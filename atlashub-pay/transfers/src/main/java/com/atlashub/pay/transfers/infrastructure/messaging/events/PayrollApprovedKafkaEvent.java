package com.atlashub.pay.transfers.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PayrollApprovedKafkaEvent(
        String eventId,
        Long aggregateId,
        Payload payload
) {
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
}
