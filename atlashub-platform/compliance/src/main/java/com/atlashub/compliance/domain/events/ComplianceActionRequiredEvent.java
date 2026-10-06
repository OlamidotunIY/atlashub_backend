package com.atlashub.compliance.domain.events;
import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;
public record ComplianceActionRequiredEvent(String eventId, Long aggregateId, ZonedDateTime occurredAt,
        String correlationId, Payload payload) implements DomainEvent<ComplianceActionRequiredEvent.Payload> {
    public record Payload(Long organizationId, String reason) {}
}
