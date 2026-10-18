package com.atlashub.hr.staff.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EmployeeTerminatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<EmployeeTerminatedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long employeeId,
            Long organizationId,
            Long userId,
            LocalDate terminationDate
    ) {}

    public static EmployeeTerminatedEvent of(
            Long employeeId,
            Long organizationId,
            Long userId,
            LocalDate terminationDate
    ) {
        return new EmployeeTerminatedEvent(
                UUID.randomUUID().toString(),
                employeeId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(
                        employeeId,
                        organizationId,
                        userId,
                        terminationDate
                )
        );
    }
}
