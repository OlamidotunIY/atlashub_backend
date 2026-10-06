package com.atlashub.compliance.infrastructure.messaging.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;
import java.time.LocalDate;
import com.atlashub.shared.domain.valueobject.Country;
import com.atlashub.shared.domain.valueobject.CurrencyCode;

public record OrganizationRegistered(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<OrganizationRegistered.Payload> {
    public record Payload(
            String businessName,
            String registrationType,
            String industry,
            LocalDate registrationDate,
            Country country,
            CurrencyCode currency,
            Long ownerUserId
    ) {
    }
}

