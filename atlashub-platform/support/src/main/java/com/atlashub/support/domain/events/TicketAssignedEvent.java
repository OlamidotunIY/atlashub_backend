package com.atlashub.support.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;
import java.util.UUID;

public record TicketAssignedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<TicketAssignedEvent.Payload> {

    public record Payload(
            Long ticketId,
            Long agentId,
            Long organizationId
    ) {}

    public static TicketAssignedEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new TicketAssignedEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
