package com.atlashub.pay.accounts.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record OrganizationAccountFundedEvent(
        String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload)
        implements DomainEvent<OrganizationAccountFundedEvent.Payload> {
    public record Payload(Long organizationId, String environment, Long businessAccountId,
                          String anchorTransferReference, BigDecimal amount, String currency,
                          ZonedDateTime receivedAt) {}
}
