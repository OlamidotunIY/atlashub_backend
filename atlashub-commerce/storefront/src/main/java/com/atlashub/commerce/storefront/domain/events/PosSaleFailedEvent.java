package com.atlashub.commerce.storefront.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PosSaleFailedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<PosSaleFailedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long salesOrderId,
            Long organizationId,
            String reason
    ) {
    }

    public static PosSaleFailedEvent of(Long salesOrderId, Long organizationId, String reason) {
        return new PosSaleFailedEvent(
                UUID.randomUUID().toString(),
                salesOrderId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(salesOrderId, organizationId, reason)
        );
    }
}
