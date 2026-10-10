package com.atlashub.pay.settlement.infrastructure.messaging.events;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record OrganizationAccountFundedEvent(
        String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload) {
    public record Payload(Long organizationId, String environment, Long businessAccountId,
                          String anchorTransferReference, BigDecimal amount, String currency,
                          ZonedDateTime receivedAt) {}
}
