package com.atlashub.support.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;
import java.util.UUID;

public record TicketEscalatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<TicketEscalatedEvent.Payload> {

    public record Payload(
            Long ticketId,
            Long newAgentId,
            String escalationReason,
            Long organizationId
    ) {}

    public static TicketEscalatedEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new TicketEscalatedEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
