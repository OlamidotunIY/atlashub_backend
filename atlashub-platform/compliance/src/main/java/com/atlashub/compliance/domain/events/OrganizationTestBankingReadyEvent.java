package com.atlashub.compliance.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record OrganizationTestBankingReadyEvent(
        String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload
) implements DomainEvent<OrganizationTestBankingReadyEvent.Payload> {
    public record Payload(
            Long organizationId,
            String anchorBusinessCustomerId,
            String environment,
            ZonedDateTime readyAt
    ) {}
}
