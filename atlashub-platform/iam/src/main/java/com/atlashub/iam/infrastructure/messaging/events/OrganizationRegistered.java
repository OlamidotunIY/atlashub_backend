package com.atlashub.iam.infrastructure.messaging.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrganizationRegistered(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<OrganizationRegistered.Payload> {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long ownerUserId
    ) {}
}

