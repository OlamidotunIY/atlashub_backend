package com.atlashub.pay.accounts.infrastructure.messaging.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;
import java.util.Set;

public record ProviderOnboardingApprovedEvent(
        String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload
) implements DomainEvent<ProviderOnboardingApprovedEvent.Payload> {
    public record Payload(
            Long onboardingCaseId,
            Long organizationId,
            String environment,
            String provider,
            Set<String> capabilities,
            String externalApplicationId,
            String externalMerchantId,
            String externalAccountId,
            String settlementAccountReference,
            ZonedDateTime approvedAt
    ) {
    }
}
