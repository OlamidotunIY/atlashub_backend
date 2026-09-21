package com.atlashub.accounts.domain.event;


import com.atlashub.accounts.domain.valueobject.BusinessSize;
import com.atlashub.accounts.domain.valueobject.BusinessType;
import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.valueobject.CurrencyCode;

import java.time.ZonedDateTime;

public record OrganizationRegistered(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    String correlationId,
    Payload payload
) implements DomainEvent<OrganizationRegistered.Payload> {
    public record Payload(
        String businessName,
        BusinessType businessType,
        BusinessSize businessSize,
        CurrencyCode currency
    ) {}
}
