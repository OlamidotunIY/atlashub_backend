package com.atlashub.charges.domain.event;

import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.money.Money;

import java.time.ZonedDateTime;

public record ExternalChargeRequestedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        Payload payload
) implements DomainEvent<ExternalChargeRequestedEvent.Payload> {
    public record Payload(
            Long purposeId,
            Long organizationId,
            Money amount,
            String customerEmail,
            String redirectUrl
    ) {}
}
