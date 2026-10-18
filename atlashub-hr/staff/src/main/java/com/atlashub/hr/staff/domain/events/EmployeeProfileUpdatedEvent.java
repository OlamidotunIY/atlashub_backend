package com.atlashub.hr.staff.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EmployeeProfileUpdatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<EmployeeProfileUpdatedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long employeeId,
            Long organizationId,
            String firstName,
            String lastName,
            String phone,
            String address,
            String nationality
    ) {}

    public static EmployeeProfileUpdatedEvent of(
            Long employeeId,
            Long organizationId,
            String firstName,
            String lastName,
            String phone,
            String address,
            String nationality
    ) {
        return new EmployeeProfileUpdatedEvent(
                UUID.randomUUID().toString(),
                employeeId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(
                        employeeId,
                        organizationId,
                        firstName,
                        lastName,
                        phone,
                        address,
                        nationality
                )
        );
    }
}
