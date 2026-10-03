package com.atlashub.pay.accounts.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;
import java.util.Set;

public record PaymentProviderProfileActivatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<PaymentProviderProfileActivatedEvent.Payload> {
    public record Payload(
            Long organizationId,
            String environment,
            String provider,
            Set<String> capabilities,
            ZonedDateTime activatedAt
    ) {
    }
}
