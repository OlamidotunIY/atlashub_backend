package com.atlashub.audit.infrastructure.messaging.events;

import java.time.ZonedDateTime;

public final class AuditInboundEvents {

    private AuditInboundEvents() {}

    // Authentication Events
    public record AuthSessionCreatedEvent(
            String eventId,
            Long aggregateId,
            Long userId,
            Long organizationId,
            String ipAddress,
            ZonedDateTime occurredAt
    ) {}

    public record AuthSessionRevokedEvent(
            String eventId,
            Long aggregateId,
            Long userId,
            Long organizationId,
            String reason,
            ZonedDateTime occurredAt
    ) {}

    public record AuthAccountLockedEvent(
            String eventId,
            Long aggregateId,
            Long userId,
            Long organizationId,
            String reason,
            ZonedDateTime occurredAt
    ) {}

    public record AuthPasswordChangedEvent(
            String eventId,
            Long aggregateId,
            Long userId,
            Long organizationId,
            ZonedDateTime occurredAt
    ) {}

    public record OrganizationBannedEvent(
            String eventId,
            Long organizationId,
            Long bannedByUserId,
            String reason,
            ZonedDateTime occurredAt
    ) {}

    // Identity Events
    public record InvitationAcceptedEvent(
            String eventId,
            Long organizationId,
            Long userId,
            Long invitationId,
            ZonedDateTime occurredAt
    ) {}

    public record OrganizationMemberAddedEvent(
            String eventId,
            Long organizationId,
            Long memberUserId,
            String role,
            ZonedDateTime occurredAt
    ) {}

    // Inventory Events
    public record StockAdjustedEvent(
            String eventId,
            Long organizationId,
            Long itemId,
            Integer quantityDelta,
            String reason,
            Long adjustedByUserId,
            ZonedDateTime occurredAt
    ) {}

    // Finance Events
    public record JournalEntryPostedEvent(
            String eventId,
            Long organizationId,
            Long journalEntryId,
            String entryNumber,
            Long postedByUserId,
            ZonedDateTime occurredAt
    ) {}
}
