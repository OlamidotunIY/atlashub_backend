package com.atlashub.logistics.shipping.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.valueobject.Money;
import java.time.ZonedDateTime;
import java.util.UUID;

public record ShipmentDeliveredEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<ShipmentDeliveredEvent.Payload> {

    public record Payload(
            Long shipmentId,
            Long organizationId,
            Long orderId,
            String trackingNumber,
            String type,
            String serviceTier,
            String proofOfDeliveryUrl,
            Money customerShippingFee,
            Money baselineSingleCost,
            Money allocatedActualCost,
            boolean incentiveEligible,
            ZonedDateTime deliveredAt
    ) {}

    public static ShipmentDeliveredEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new ShipmentDeliveredEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
