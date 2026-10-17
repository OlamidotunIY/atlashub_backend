package com.atlashub.logistics.returns.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.valueobject.Money;
import java.time.ZonedDateTime;
import java.util.UUID;

public record ReturnShipmentReceivedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<ReturnShipmentReceivedEvent.Payload> {

    public record Payload(
            Long returnShipmentId,
            Long organizationId,
            Long originalSalesOrderId,
            Long customerId,
            String faultType,
            Money customerRefundAmount,
            Money merchantPenaltyAmount,
            Money merchantCompensationAmount,
            boolean refundCustomerShipping,
            String inspectionNotes,
            ZonedDateTime receivedAt
    ) {}

    public static ReturnShipmentReceivedEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new ReturnShipmentReceivedEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
