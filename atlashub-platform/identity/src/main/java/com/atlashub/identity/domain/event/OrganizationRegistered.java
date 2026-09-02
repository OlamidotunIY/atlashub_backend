package com.atlashub.identity.domain.event;

import com.atlashub.identity.domain.valueobject.BusinessSize;
import com.atlashub.identity.domain.valueobject.BusinessType;
import com.atlashub.shared.event.DomainEvent;
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
        BusinessSize businessSize
    ) {}
}
