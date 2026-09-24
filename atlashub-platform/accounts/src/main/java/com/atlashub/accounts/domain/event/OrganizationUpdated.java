package com.atlashub.accounts.domain.event;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record OrganizationUpdated(
    String eventId,
    Long aggregateId,
    ZonedDateTime occurredAt,
    String correlationId,
    Payload payload
) implements DomainEvent<OrganizationUpdated.Payload> {
    public record Payload(
        String businessName,
        String description,
        String logoUrl,
        String industry,
        String websiteUrl
    ) {}
}
