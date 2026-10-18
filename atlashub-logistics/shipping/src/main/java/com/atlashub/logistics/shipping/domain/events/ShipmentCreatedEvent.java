package com.atlashub.logistics.shipping.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public record ShipmentCreatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<ShipmentCreatedEvent.Payload> {

    public record Payload(
            Long shipmentId,
            Long organizationId,
            Long orderId,
            String trackingNumber,
            String type,
            String serviceTier,
            String routeType,
            BigDecimal weightKg,
            Money customerShippingFee,
            ZonedDateTime deliveryWindowStart,
            ZonedDateTime deliveryWindowEnd,
            ZonedDateTime latestDispatchAt,
            ZonedDateTime createdAt
    ) {}

    public static ShipmentCreatedEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new ShipmentCreatedEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
