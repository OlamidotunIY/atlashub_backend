package com.atlashub.logistics.returns.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.valueobject.Money;
import java.time.ZonedDateTime;
import java.util.UUID;

public record ReturnShipmentCreatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<ReturnShipmentCreatedEvent.Payload> {

    public record Payload(
            Long returnShipmentId,
            Long organizationId,
            Long originalSalesOrderId,
            Long originalShipmentId,
            Long customerId,
            String returnTrackingNumber,
            String reasonType,
            String faultType,
            Money originalItemAmount,
            Money originalShippingFee,
            Money returnShippingFee,
            Money customerRefundAmount,
            Money merchantPenaltyAmount,
            Money merchantCompensationAmount,
            boolean refundCustomerShipping,
            String status,
            ZonedDateTime createdAt
    ) {}

    public static ReturnShipmentCreatedEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new ReturnShipmentCreatedEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
