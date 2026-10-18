package com.atlashub.pay.mandates.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MandateExpiredEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<MandateExpiredEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long mandateId,
            Long organizationId,
            String customerId,
            String email,
            String reason,
            ZonedDateTime expiredAt
    ) {}

    public static MandateExpiredEvent of(
            Long mandateId,
            Long organizationId,
            String customerId,
            String email,
            String reason,
            ZonedDateTime expiredAt
    ) {
        return new MandateExpiredEvent(
                UUID.randomUUID().toString(),
                mandateId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(
                        mandateId,
                        organizationId,
                        customerId,
                        email,
                        reason,
                        expiredAt
                )
        );
    }
}
