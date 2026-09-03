package com.atlashub.charges.domain.event;

import com.atlashub.charges.domain.valueobject.ChargePurpose;
import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record ExternalPaymentFailedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        Payload payload
) implements DomainEvent<ExternalPaymentFailedEvent.Payload> {
    public record Payload(
            String reference,
            ChargePurpose purpose,
            Long purposeId,
            String reason
    ) {}
}
