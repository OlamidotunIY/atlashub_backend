package com.atlashub.support.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.support.domain.valueobject.AuthorType;
import java.time.ZonedDateTime;
import java.util.UUID;

public record TicketMessageAddedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<TicketMessageAddedEvent.Payload> {

    public record Payload(
            Long ticketId,
            Long authorId,
            AuthorType authorType,
            boolean isInternalNote
    ) {}

    public static TicketMessageAddedEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new TicketMessageAddedEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
