package com.atlashub.support.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;
import java.util.UUID;

public record TicketResolvedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<TicketResolvedEvent.Payload> {

    public record Payload(
            Long ticketId,
            Long agentId,
            String resolutionNote,
            Long organizationId
    ) {}

    public static TicketResolvedEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new TicketResolvedEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
