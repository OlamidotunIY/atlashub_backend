package com.atlashub.identity.domain.event;

import com.atlashub.identity.domain.valueobject.BusinessSize;
import com.atlashub.identity.domain.valueobject.BusinessType;
import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.money.CurrencyCode;

import java.time.ZonedDateTime;

public record OrganizationRegistered(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<OrganizationRegistered.Payload> {
    public record Payload(
        String businessName,
        BusinessType businessType,
        BusinessSize businessSize,
        CurrencyCode currency
    ) {}
}
