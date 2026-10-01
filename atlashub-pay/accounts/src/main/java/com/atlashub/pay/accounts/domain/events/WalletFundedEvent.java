package com.atlashub.pay.accounts.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.ZonedDateTime;

public record WalletFundedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<WalletFundedEvent.Payload> {

    public record Payload(
            Long virtualAccountId,
            Long organizationId,
            String ownerType,
            String customerId,
            String transactionReference,
            Money amount,
            String senderAccountName,
            String senderBankCode,
            String orgAdminEmail,
            String customerEmail,
            ZonedDateTime fundedAt
    ) {
    }
}
