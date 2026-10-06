package com.atlashub.iam.infrastructure.messaging.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record EmployeeSuspended(
        String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload
) implements DomainEvent<EmployeeSuspended.Payload> {
    public record Payload(Long employeeId, Long organizationId, Long userId, String reason,
                          ZonedDateTime suspendedAt) {}
}
