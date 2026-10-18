package com.atlashub.pay.transfers.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record PayoutApprovedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<PayoutApprovedEvent.Payload> {

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
