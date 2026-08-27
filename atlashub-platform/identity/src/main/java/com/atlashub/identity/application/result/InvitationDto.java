package com.atlashub.identity.application.result;

import com.atlashub.identity.domain.valueobject.InvitationStatus;
import com.atlashub.identity.domain.valueobject.OrganizationRole;
import java.time.ZonedDateTime;
import java.util.UUID;

public record InvitationDto(
    UUID id,
    Long organizationId,
    String invitedEmail,
    OrganizationRole role,
    InvitationStatus status,
    ZonedDateTime expiresAt
) {}
