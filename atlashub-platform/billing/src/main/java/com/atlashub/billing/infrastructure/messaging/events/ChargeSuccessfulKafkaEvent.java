package com.atlashub.billing.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChargeSuccessfulKafkaEvent(
        String eventId,
        Long aggregateId,
        Payload payload
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long organizationId,
            String chargeReference,
            MoneyPayload amount,
            String sourceSystem,
            String sourceReferenceId
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MoneyPayload(
            BigDecimal amount,
            String currency
    ) {
    }
}
