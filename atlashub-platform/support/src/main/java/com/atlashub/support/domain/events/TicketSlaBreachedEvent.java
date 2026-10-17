package com.atlashub.support.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.support.domain.valueobject.TicketPriority;
import java.time.ZonedDateTime;
import java.util.UUID;

public record TicketSlaBreachedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<TicketSlaBreachedEvent.Payload> {

    public record Payload(
            Long ticketId,
            String ticketNumber,
            Long organizationId,
            TicketPriority priority,
            String breachType
    ) {}

    public static TicketSlaBreachedEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new TicketSlaBreachedEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
