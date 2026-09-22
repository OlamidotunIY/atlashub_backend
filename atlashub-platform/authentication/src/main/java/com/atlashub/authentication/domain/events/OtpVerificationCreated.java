package com.atlashub.authentication.domain.events;

import com.atlashub.authentication.domain.valueobject.OtpType;
import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record OtpVerificationCreated(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<OtpVerificationCreated.Payload> {
    public record Payload(
            OtpType type,
            ZonedDateTime expiresAt
    ) {
    }
}
