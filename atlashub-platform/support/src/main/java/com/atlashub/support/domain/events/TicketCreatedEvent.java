package com.atlashub.support.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.support.domain.valueobject.TicketCategory;
import com.atlashub.support.domain.valueobject.TicketPriority;
import java.time.ZonedDateTime;
import java.util.UUID;

public record TicketCreatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<TicketCreatedEvent.Payload> {

    public record Payload(
            Long ticketId,
            String ticketNumber,
            Long organizationId,
            Long raisedBy,
            TicketCategory category,
            TicketPriority priority,
            String subject
    ) {}

    public static TicketCreatedEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new TicketCreatedEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
