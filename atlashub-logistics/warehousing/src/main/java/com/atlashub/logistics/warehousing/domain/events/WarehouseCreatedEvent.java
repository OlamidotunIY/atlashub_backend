package com.atlashub.logistics.warehousing.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;
import java.util.UUID;

public record WarehouseCreatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<WarehouseCreatedEvent.Payload> {

    public record Payload(
            Long warehouseId,
            Long organizationId,
            String name,
            String code,
            String facilityType,
            ZonedDateTime createdAt
    ) {}

    public static WarehouseCreatedEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new WarehouseCreatedEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
