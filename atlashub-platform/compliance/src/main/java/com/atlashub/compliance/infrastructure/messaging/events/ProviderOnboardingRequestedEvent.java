package com.atlashub.compliance.infrastructure.messaging.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;
import java.util.Set;

public record ProviderOnboardingRequestedEvent(
        String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload
) implements DomainEvent<ProviderOnboardingRequestedEvent.Payload> {
    public record Payload(
            Long organizationId,
            String environment,
            String provider,
            Set<String> capabilities,
            ZonedDateTime requestedAt
    ) {
    }
}
