package com.atlashub.analytics.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EmployeeSuspendedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<EmployeeSuspendedEvent.Payload> {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(Long employeeId, Long organizationId, Long userId, String reason) {
    }
}
