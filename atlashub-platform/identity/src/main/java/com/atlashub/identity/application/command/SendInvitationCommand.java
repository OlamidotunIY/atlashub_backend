package com.atlashub.identity.application.command;

import com.atlashub.identity.domain.valueobject.OrganizationRole;

public record SendInvitationCommand(
    Long organizationId,
    String email,
    OrganizationRole role,
    Long inviterId
) {}
