package com.atlashub.pay.mandates.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MandatePausedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<MandatePausedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long mandateId,
            Long organizationId,
            String customerId,
            String email,
            ZonedDateTime pausedAt
    ) {}

    public static MandatePausedEvent of(
            Long mandateId,
            Long organizationId,
            String customerId,
            String email,
            ZonedDateTime pausedAt
    ) {
        return new MandatePausedEvent(
                UUID.randomUUID().toString(),
                mandateId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(
                        mandateId,
                        organizationId,
                        customerId,
                        email,
                        pausedAt
                )
        );
    }
}
