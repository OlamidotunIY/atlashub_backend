package com.atlashub.hr.staff.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EmployeePromotedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<EmployeePromotedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long employeeId,
            Long organizationId,
            String newDesignation,
            Long newGradeId,
            BigDecimal newBaseSalary,
            String currency
    ) {}

    public static EmployeePromotedEvent of(
            Long employeeId,
            Long organizationId,
            String newDesignation,
            Long newGradeId,
            BigDecimal newBaseSalary,
            String currency
    ) {
        return new EmployeePromotedEvent(
                UUID.randomUUID().toString(),
                employeeId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(
                        employeeId,
                        organizationId,
                        newDesignation,
                        newGradeId,
                        newBaseSalary,
                        currency
                )
        );
    }
}
