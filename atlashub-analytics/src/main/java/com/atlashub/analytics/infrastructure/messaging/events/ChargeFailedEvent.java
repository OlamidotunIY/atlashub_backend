package com.atlashub.analytics.infrastructure.messaging.events;

import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChargeFailedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<ChargeFailedEvent.Payload> {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long organizationId,
            String environment,
            String chargeReference,
            Money amount,
            String channel,
            String provider,
            String sourceSystem,
            String sourceReferenceId,
            String customerReferenceId,
            String reason,
            ZonedDateTime failedAt
    ) {
    }
}
