package com.atlashub.commerce.catalog.domain.events;

import com.atlashub.commerce.catalog.domain.valueobject.ProductType;
import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record ProductCreatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<ProductCreatedEvent.Payload> {

    public record Payload(
            Long organizationId,
            Long vendorId,
            String code,
            String name,
            ProductType type,
            boolean service,
            ZonedDateTime createdAt
    ) {
    }
}
