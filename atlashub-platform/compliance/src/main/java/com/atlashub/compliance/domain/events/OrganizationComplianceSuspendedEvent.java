package com.atlashub.compliance.domain.events;
import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;
public record OrganizationComplianceSuspendedEvent(String eventId, Long aggregateId, ZonedDateTime occurredAt,
        String correlationId, Payload payload) implements DomainEvent<OrganizationComplianceSuspendedEvent.Payload> {
    public record Payload(Long organizationId, String anchorBusinessCustomerId, String reason, ZonedDateTime suspendedAt) {}
}
