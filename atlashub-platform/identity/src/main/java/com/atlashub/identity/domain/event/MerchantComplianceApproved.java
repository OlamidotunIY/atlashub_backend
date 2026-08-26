package com.atlashub.identity.domain.event;

import com.atlashub.shared.domain.valueobject.Country;
import com.atlashub.shared.event.DomainEvent;

import java.time.ZonedDateTime;

public record MerchantComplianceApproved(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<MerchantComplianceApproved.Payload> {
    public record Payload(String merchantName, Country country) {}
}
