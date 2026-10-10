package com.atlashub.commerce.catalog.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record ProductApprovedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<ProductApprovedEvent.Payload> {

    public record Payload(
            Long organizationId,
            Long vendorId,
            String code,
            String name,
            ZonedDateTime approvedAt
    ) {
    }
}
