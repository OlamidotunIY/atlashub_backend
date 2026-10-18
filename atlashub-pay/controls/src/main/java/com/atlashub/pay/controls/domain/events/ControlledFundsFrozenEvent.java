package com.atlashub.pay.controls.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record ControlledFundsFrozenEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<ControlledFundsFrozenEvent.Payload> {

    public record Payload(
            Long decisionId,
            Long facilityId,
            Long organizationId,
            String accountReference,
            BigDecimal amount,
            String currency,
            ZonedDateTime frozenAt
    ) {}
}
