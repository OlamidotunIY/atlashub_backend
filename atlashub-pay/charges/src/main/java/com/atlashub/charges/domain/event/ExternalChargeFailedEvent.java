package com.atlashub.charges.domain.event;

import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.charges.domain.valueobject.ChargePurpose;
import java.time.ZonedDateTime;

public record ExternalChargeFailedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        Payload payload
) implements DomainEvent<ExternalChargeFailedEvent.Payload> {
    public record Payload(
            String reference,
            ChargePurpose purpose,
            String metadata
    ) {}
}
