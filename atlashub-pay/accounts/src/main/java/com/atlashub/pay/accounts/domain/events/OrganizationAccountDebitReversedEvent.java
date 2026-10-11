package com.atlashub.pay.accounts.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record OrganizationAccountDebitReversedEvent(
        String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload)
        implements DomainEvent<OrganizationAccountDebitReversedEvent.Payload> {

    public record Payload(Long organizationId, String environment, Long businessAccountId,
                          String anchorTransferId, String transferReference, BigDecimal amount, String currency,
                          String narration, ZonedDateTime reversedAt) {
    }
}
