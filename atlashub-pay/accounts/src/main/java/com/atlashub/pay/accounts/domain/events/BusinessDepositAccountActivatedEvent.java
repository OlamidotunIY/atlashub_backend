package com.atlashub.pay.accounts.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record BusinessDepositAccountActivatedEvent(
        String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload)
        implements DomainEvent<BusinessDepositAccountActivatedEvent.Payload> {
    public record Payload(Long organizationId, Long bankingProfileId, String environment,
                          String currency, ZonedDateTime activatedAt) {}
}
