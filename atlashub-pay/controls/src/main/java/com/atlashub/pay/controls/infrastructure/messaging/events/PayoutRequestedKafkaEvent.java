package com.atlashub.pay.controls.infrastructure.messaging.events;

import java.math.BigDecimal;

public record PayoutRequestedKafkaEvent(
        String eventId,
        Long aggregateId,
        String eventType,
        Payload payload
) {
    public record Payload(
            Long payoutId,
            Long organizationId,
            Long facilityId,
            String reference,
            BigDecimal amount,
            String currency,
            String recipientBankCode,
            String recipientAccountNumber,
            String categoryCode
    ) {}
}
