package com.atlashub.iam.application.queries.ListInvitations;

import java.time.ZonedDateTime;

public record InvitationResult(
    Long id,
    Long organizationId,
    String invitedEmail,
    String status,
    ZonedDateTime expiresAt,
    ZonedDateTime createdAt
) {}
