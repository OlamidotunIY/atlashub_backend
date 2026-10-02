package com.atlashub.accounts.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.accounts.domain.valueobject.SupportedIndustry;

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
        SupportedIndustry industry,
        String websiteUrl
    ) {}
}
