package com.atlashub.hr.staff.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InfractionResolvedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<InfractionResolvedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long infractionId,
            Long employeeId,
            Long organizationId,
            String resolutionNotes,
            ZonedDateTime resolvedAt
    ) {}

    public static InfractionResolvedEvent of(
            Long infractionId,
            Long employeeId,
            Long organizationId,
            String resolutionNotes,
            ZonedDateTime resolvedAt
    ) {
        return new InfractionResolvedEvent(
                UUID.randomUUID().toString(),
                infractionId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(
                        infractionId,
                        employeeId,
                        organizationId,
                        resolutionNotes,
                        resolvedAt
                )
        );
    }
}
