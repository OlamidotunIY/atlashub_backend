package com.atlashub.analytics.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.atlashub.shared.domain.event.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

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
    ) {
    }
}
