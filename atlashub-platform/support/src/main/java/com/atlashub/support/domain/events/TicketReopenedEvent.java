package com.atlashub.support.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;
import java.util.UUID;

public record TicketReopenedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<TicketReopenedEvent.Payload> {

    public record Payload(
            Long ticketId,
            Long userId,
            String reason,
            Long organizationId
    ) {}

    public static TicketReopenedEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new TicketReopenedEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
