package com.atlashub.pay.transfers.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PayoutApprovedKafkaEvent(
        String eventId,
        Long aggregateId,
        Payload payload
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long payoutId,
            Long organizationId,
            BigDecimal amount,
            String currency,
            Long approvedBy,
            ZonedDateTime approvedAt
    ) {
    }
}
