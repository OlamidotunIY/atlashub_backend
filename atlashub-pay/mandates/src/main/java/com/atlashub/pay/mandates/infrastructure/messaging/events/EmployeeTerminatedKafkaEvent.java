package com.atlashub.pay.mandates.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EmployeeTerminatedKafkaEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long employeeId,
            Long organizationId,
            Long userId,
            LocalDate terminationDate
    ) {}
}
