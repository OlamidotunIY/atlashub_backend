package com.atlashub.pay.transfers.infrastructure.messaging.events;

import com.atlashub.shared.domain.valueobject.Money;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChargeRefundInitiatedKafkaEvent(
        String eventId,
        Long aggregateId,
        Payload payload
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long organizationId,
            String environment,
            String chargeReference,
            Money amount,
            String sourceSystem,
            String sourceReferenceId,
            String customerReferenceId,
            String reason,
            ZonedDateTime initiatedAt
    ) {
    }
}
