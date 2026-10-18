package com.atlashub.hr.staff.domain.events;

import com.atlashub.hr.staff.domain.valueobject.InfractionSeverity;
import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InfractionLoggedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<InfractionLoggedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long infractionId,
            Long employeeId,
            Long organizationId,
            String description,
            InfractionSeverity severity,
            Long reportedBy
    ) {}

    public static InfractionLoggedEvent of(
            Long infractionId,
            Long employeeId,
            Long organizationId,
            String description,
            InfractionSeverity severity,
            Long reportedBy
    ) {
        return new InfractionLoggedEvent(
                UUID.randomUUID().toString(),
                infractionId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(
                        infractionId,
                        employeeId,
                        organizationId,
                        description,
                        severity,
                        reportedBy
                )
        );
    }
}
