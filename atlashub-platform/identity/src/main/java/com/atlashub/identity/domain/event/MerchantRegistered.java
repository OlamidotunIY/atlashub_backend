package com.atlashub.identity.domain.event;

import com.atlashub.identity.domain.model.BusinessType;
import com.atlashub.shared.event.DomainEvent;

import java.time.ZonedDateTime;

public record MerchantRegistered(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<MerchantRegistered.Payload> {
    public record Payload(
        String businessName,
        String email,
        String country,
        BusinessType businessType
    ) {}
}
