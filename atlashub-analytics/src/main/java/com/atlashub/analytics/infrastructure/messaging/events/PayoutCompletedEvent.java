package com.atlashub.analytics.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.atlashub.shared.domain.event.DomainEvent;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PayoutCompletedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<PayoutCompletedEvent.Payload> {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long payoutId,
            Long organizationId,
            String reference,
            String providerReference,
            String sourceSystem,
            String sourceReferenceId,
            Long recipientUserId,
            BigDecimal amount,
            String currency,
            ZonedDateTime completedAt
    ) {
    }
}
