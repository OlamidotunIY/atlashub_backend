package com.atlashub.hr.staff.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EmployeeOnboardedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<EmployeeOnboardedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long employeeId,
            Long organizationId,
            Long userId,
            String employeeNumber,
            String firstName,
            String lastName,
            String email,
            String designation,
            String department,
            LocalDate hireDate,
            BigDecimal baseSalary,
            String currency
    ) {}

    public static EmployeeOnboardedEvent of(
            Long employeeId,
            Long organizationId,
            Long userId,
            String employeeNumber,
            String firstName,
            String lastName,
            String email,
            String designation,
            String department,
            LocalDate hireDate,
            BigDecimal baseSalary,
            String currency
    ) {
        return new EmployeeOnboardedEvent(
                UUID.randomUUID().toString(),
                employeeId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(
                        employeeId,
                        organizationId,
                        userId,
                        employeeNumber,
                        firstName,
                        lastName,
                        email,
                        designation,
                        department,
                        hireDate,
                        baseSalary,
                        currency
                )
        );
    }
}
