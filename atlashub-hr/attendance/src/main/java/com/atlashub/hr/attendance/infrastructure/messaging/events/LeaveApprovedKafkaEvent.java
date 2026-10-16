package com.atlashub.hr.attendance.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LeaveApprovedKafkaEvent(
        String eventId,
        Long aggregateId,
        Payload payload
) {
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
}
