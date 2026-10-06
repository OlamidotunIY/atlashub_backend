package com.atlashub.pay.accounts.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record OrganizationBankingProvisioningFailedEvent(
        String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload)
        implements DomainEvent<OrganizationBankingProvisioningFailedEvent.Payload> {
    public record Payload(Long organizationId, String environment, String failureCode,
                          String failureMessage, ZonedDateTime failedAt) {}
}
