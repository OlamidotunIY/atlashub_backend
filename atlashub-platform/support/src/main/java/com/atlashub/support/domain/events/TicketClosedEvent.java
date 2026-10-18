package com.atlashub.support.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;
import java.util.UUID;

public record TicketClosedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<TicketClosedEvent.Payload> {

    public record Payload(
            Long ticketId,
            Long closedBy,
            Long organizationId
    ) {}

    public static TicketClosedEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new TicketClosedEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
