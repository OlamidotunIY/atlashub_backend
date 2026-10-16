package com.atlashub.pay.transfers.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record BulkPayoutCompletedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<BulkPayoutCompletedEvent.Payload> {

    public record Payload(
            Long payrollRunId,
            Long organizationId,
            BigDecimal totalAmountDisbursed,
            ZonedDateTime disbursedAt
    ) {
    }
}
