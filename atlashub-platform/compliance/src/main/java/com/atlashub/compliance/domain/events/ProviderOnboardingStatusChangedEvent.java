package com.atlashub.compliance.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record ProviderOnboardingStatusChangedEvent(
        String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload
) implements DomainEvent<ProviderOnboardingStatusChangedEvent.Payload> {
    public record Payload(
            Long onboardingCaseId,
            Long organizationId,
            String environment,
            String provider,
            String status,
            String failureCode,
            String failureMessage,
            ZonedDateTime changedAt
    ) {
    }
}
