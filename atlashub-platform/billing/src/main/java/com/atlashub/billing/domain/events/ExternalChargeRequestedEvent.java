package com.atlashub.billing.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record ExternalChargeRequestedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        Payload payload
) implements DomainEvent<ExternalChargeRequestedEvent.Payload> {
    public record Payload(
            Long invoiceId,
            Long organizationId,
            BigDecimal amount,
            String currency,
            String customerEmail,
            String redirectUrl
    ) {}
}