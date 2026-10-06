package com.atlashub.accounts.domain.events;


import com.atlashub.accounts.domain.valueobject.AtlasHubRegistrationType;
import com.atlashub.accounts.domain.valueobject.SupportedIndustry;
import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.valueobject.Country;
import com.atlashub.shared.domain.valueobject.CurrencyCode;

import java.time.ZonedDateTime;
import java.time.LocalDate;

public record OrganizationRegistered(
    String eventId,
    Long aggregateId,
    ZonedDateTime occurredAt,
    String correlationId,
    Payload payload
) implements DomainEvent<OrganizationRegistered.Payload> {
    public record Payload(
        String businessName,
        AtlasHubRegistrationType registrationType,
        SupportedIndustry industry,
        LocalDate registrationDate,
        Country country,
        CurrencyCode currency,
        Long ownerUserId
    ) {}
}
