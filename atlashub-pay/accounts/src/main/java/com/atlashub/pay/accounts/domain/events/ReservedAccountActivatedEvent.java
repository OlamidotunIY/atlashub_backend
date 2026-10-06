package com.atlashub.pay.accounts.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record ReservedAccountActivatedEvent(
        String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload)
        implements DomainEvent<ReservedAccountActivatedEvent.Payload> {
    public record Payload(Long reservedAccountId, Long organizationId, String ownerType,
                          String ownerReferenceId, Long businessSubAccountId,
                          String anchorReservedAccountId, String accountName,
                          String maskedAccountNumber, String bankName, String environment, String currency,
                          ZonedDateTime activatedAt) {
    }
}
