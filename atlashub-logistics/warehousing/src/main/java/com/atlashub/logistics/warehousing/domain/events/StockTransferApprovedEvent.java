package com.atlashub.logistics.warehousing.domain.events;

import com.atlashub.logistics.warehousing.domain.valueobject.Address;
import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public record StockTransferApprovedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<StockTransferApprovedEvent.Payload> {

    public record Payload(
            Long stockTransferId,
            Long organizationId,
            Long sourceId,
            Address sourceAddress,
            Long destinationId,
            Address destinationAddress,
            BigDecimal totalWeightKg,
            Money declaredValue,
            Long requestedBy,
            Long approvedBy,
            ZonedDateTime approvedAt
    ) {}

    public static StockTransferApprovedEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new StockTransferApprovedEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
