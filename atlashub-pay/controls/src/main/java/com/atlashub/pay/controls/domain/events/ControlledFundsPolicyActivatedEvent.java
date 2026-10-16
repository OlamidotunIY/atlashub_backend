package com.atlashub.pay.controls.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record ControlledFundsPolicyActivatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<ControlledFundsPolicyActivatedEvent.Payload> {

    public record Payload(
            Long policyId,
            Long facilityId,
            Long organizationId,
            String policyVersion,
            String mode,
            ZonedDateTime activatedAt,
            String activatedBy
    ) {}
}
