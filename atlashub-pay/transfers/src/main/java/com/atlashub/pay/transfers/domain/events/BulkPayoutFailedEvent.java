package com.atlashub.pay.transfers.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record BulkPayoutFailedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<BulkPayoutFailedEvent.Payload> {

    public record Payload(
            Long payrollRunId,
            Long organizationId,
            String failureReason
    ) {
    }
}
