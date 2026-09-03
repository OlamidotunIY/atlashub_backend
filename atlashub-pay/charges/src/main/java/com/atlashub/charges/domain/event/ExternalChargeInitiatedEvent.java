package com.atlashub.charges.domain.event;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record ExternalChargeInitiatedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        Payload payload
) implements DomainEvent<ExternalChargeInitiatedEvent.Payload> {
    public record Payload(
            Long purposeId,
            String reference,
            String checkoutUrl
    ) {}
}
