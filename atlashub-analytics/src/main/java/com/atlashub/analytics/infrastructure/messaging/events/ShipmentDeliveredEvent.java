package com.atlashub.analytics.infrastructure.messaging.events;

import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ShipmentDeliveredEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<ShipmentDeliveredEvent.Payload> {
    @JsonIgnoreProperties(ignoreUnknown = true)
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
    ) {
    }
}
