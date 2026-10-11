package com.atlashub.pay.accounts.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record OrganizationAccountDebitedEvent(
        String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload)
        implements DomainEvent<OrganizationAccountDebitedEvent.Payload> {

    public record Payload(Long organizationId, String environment, Long businessAccountId,
                          String anchorTransferId, String transferReference, BigDecimal amount, String currency,
                          String recipientName, String maskedRecipientAccountNumber, String recipientBankCode,
                          String narration, ZonedDateTime completedAt) {
    }
}
