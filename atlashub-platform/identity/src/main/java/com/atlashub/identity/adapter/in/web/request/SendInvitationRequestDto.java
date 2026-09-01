package com.atlashub.identity.adapter.in.web.request;

import com.atlashub.identity.domain.valueobject.OrganizationRole;

public record SendInvitationRequestDto(
    String email,
    OrganizationRole role
) {}
