package com.atlashub.hr.leave.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LeaveApprovedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<LeaveApprovedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long leaveApplicationId,
            Long employeeId,
            Long organizationId,
            Long leaveTypeId,
            LocalDate startDate,
            LocalDate endDate,
            Integer daysRequested,
            Long approvedBy
    ) {}

    public static LeaveApprovedEvent of(
            Long leaveApplicationId,
            Long employeeId,
            Long organizationId,
            Long leaveTypeId,
            LocalDate startDate,
            LocalDate endDate,
            Integer daysRequested,
            Long approvedBy
    ) {
        return new LeaveApprovedEvent(
                UUID.randomUUID().toString(),
                leaveApplicationId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(
                        leaveApplicationId,
                        employeeId,
                        organizationId,
                        leaveTypeId,
                        startDate,
                        endDate,
                        daysRequested,
                        approvedBy
                )
        );
    }
}
