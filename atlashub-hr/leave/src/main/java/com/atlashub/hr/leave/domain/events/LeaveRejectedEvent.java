package com.atlashub.hr.leave.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LeaveRejectedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<LeaveRejectedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long leaveApplicationId,
            Long employeeId,
            Long organizationId,
            Long leaveTypeId,
            String reason
    ) {}

    public static LeaveRejectedEvent of(
            Long leaveApplicationId,
            Long employeeId,
            Long organizationId,
            Long leaveTypeId,
            String reason
    ) {
        return new LeaveRejectedEvent(
                UUID.randomUUID().toString(),
                leaveApplicationId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(
                        leaveApplicationId,
                        employeeId,
                        organizationId,
                        leaveTypeId,
                        reason
                )
        );
    }
}
