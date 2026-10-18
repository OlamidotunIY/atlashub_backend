package com.atlashub.analytics.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.atlashub.shared.domain.event.DomainEvent;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EmployeeTerminatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<EmployeeTerminatedEvent.Payload> {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(Long employeeId, Long organizationId, Long userId, LocalDate terminationDate) {
    }
}
