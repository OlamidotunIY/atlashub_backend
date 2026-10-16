package com.atlashub.pay.controls.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record PaymentBlockedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<PaymentBlockedEvent.Payload> {

    public record Payload(
            Long decisionId,
            Long policyId,
            Long facilityId,
            Long organizationId,
            String paymentInstructionReference,
            String reason,
            ZonedDateTime blockedAt
    ) {}
}
