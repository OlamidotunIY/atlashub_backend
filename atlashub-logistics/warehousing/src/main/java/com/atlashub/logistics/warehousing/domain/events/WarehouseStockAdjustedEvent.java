package com.atlashub.logistics.warehousing.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;
import java.util.UUID;

public record WarehouseStockAdjustedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<WarehouseStockAdjustedEvent.Payload> {

    public record Payload(
            Long inventoryId,
            Long warehouseId,
            Long productId,
            int quantity,
            int reservedQuantity,
            ZonedDateTime updatedAt
    ) {}

    public static WarehouseStockAdjustedEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new WarehouseStockAdjustedEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
