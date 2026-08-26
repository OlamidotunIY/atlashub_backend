package com.atlashub.identity.domain.event;

import com.atlashub.shared.event.DomainEvent;

import java.time.ZonedDateTime;

public record MerchantComplianceRejected(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<MerchantComplianceRejected.Payload> {
    public record Payload(String reason) {}
}
