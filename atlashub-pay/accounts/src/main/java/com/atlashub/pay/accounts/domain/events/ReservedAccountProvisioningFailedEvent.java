package com.atlashub.pay.accounts.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record ReservedAccountProvisioningFailedEvent(
        String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload)
        implements DomainEvent<ReservedAccountProvisioningFailedEvent.Payload> {
    public record Payload(Long organizationId, String environment, String failureReason,
                          ZonedDateTime failedAt) {}
}
