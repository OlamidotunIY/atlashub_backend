package com.atlashub.pay.controls.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;
import java.util.Set;

public record PaymentControlEvaluatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<PaymentControlEvaluatedEvent.Payload> {

    public record Payload(
            Long decisionId,
            Long policyId,
            Long facilityId,
            Long organizationId,
            String paymentInstructionReference,
            String evaluatedAction,
            String enforcedAction,
            Set<String> reasons,
            ZonedDateTime evaluatedAt
    ) {}
}
