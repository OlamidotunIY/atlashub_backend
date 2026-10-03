package com.atlashub.pay.accounts.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record OrganizationBankingActivatedEvent(
        String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload)
        implements DomainEvent<OrganizationBankingActivatedEvent.Payload> {
    public record Payload(Long organizationId, Long bankingProfileId, Long businessDepositAccountId,
                          Long businessSubAccountId, String environment, String currency, ZonedDateTime activatedAt) {
    }
}
