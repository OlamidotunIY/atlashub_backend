package com.atlashub.authentication.infrastructure.messaging.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record CustomRolePermissionsChanged(
        String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload
) implements DomainEvent<CustomRolePermissionsChanged.Payload> {
    public record Payload(Long organizationId) {}
}
