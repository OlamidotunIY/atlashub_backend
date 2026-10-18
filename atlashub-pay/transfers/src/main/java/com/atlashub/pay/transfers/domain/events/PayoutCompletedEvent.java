package com.atlashub.pay.transfers.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record PayoutCompletedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<PayoutCompletedEvent.Payload> {

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
