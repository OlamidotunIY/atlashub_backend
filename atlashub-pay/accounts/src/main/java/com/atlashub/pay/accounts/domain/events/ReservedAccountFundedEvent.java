package com.atlashub.pay.accounts.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record ReservedAccountFundedEvent(
        String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload)
        implements DomainEvent<ReservedAccountFundedEvent.Payload> {
    public record Payload(Long reservedAccountId, Long organizationId, String environment, String ownerType,
                          String ownerReferenceId, Long businessSubAccountId, String anchorTransferReference,
                          BigDecimal amount, String currency, String senderAccountName,
                          String senderBankCode, ZonedDateTime receivedAt) {}
}
