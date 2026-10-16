package com.atlashub.pay.transfers.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record PayoutFailedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<PayoutFailedEvent.Payload> {

    public record Payload(
            Long payoutId,
            Long organizationId,
            String reference,
            String failureReason,
            String sourceSystem,
            String sourceReferenceId,
            BigDecimal amount,
            String currency,
            ZonedDateTime failedAt
    ) {
    }
}
