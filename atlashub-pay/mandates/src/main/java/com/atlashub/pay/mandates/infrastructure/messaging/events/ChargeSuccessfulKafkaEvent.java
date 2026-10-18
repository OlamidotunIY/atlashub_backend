package com.atlashub.pay.mandates.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChargeSuccessfulKafkaEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long organizationId,
            String environment,
            String chargeReference,
            String gatewayReference,
            String channel,
            String provider,
            String sourceSystem,
            String sourceReferenceId,
            String customerReferenceId,
            ZonedDateTime succeededAt
    ) {}
}
