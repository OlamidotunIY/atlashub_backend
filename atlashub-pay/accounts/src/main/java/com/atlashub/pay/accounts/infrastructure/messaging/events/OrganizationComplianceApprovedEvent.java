package com.atlashub.pay.accounts.infrastructure.messaging.events;

import java.time.ZonedDateTime;

public record OrganizationComplianceApprovedEvent(
        String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload) {
    public record Payload(Long organizationId, String anchorBusinessCustomerId, ZonedDateTime approvedAt) {
    }
}
