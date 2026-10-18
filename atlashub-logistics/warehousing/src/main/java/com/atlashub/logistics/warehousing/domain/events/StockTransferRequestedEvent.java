package com.atlashub.logistics.warehousing.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public record StockTransferRequestedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<StockTransferRequestedEvent.Payload> {

    public record Payload(
            Long stockTransferId,
            Long organizationId,
            Long sourceId,
            String sourceType,
            Long destinationId,
            String destinationType,
            BigDecimal totalWeightKg,
            Money transferShippingFee,
            Long requestedBy,
            ZonedDateTime requestedAt
    ) {}

    public static StockTransferRequestedEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new StockTransferRequestedEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
