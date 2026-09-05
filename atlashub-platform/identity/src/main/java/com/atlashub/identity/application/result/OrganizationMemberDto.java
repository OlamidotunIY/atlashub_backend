package com.atlashub.identity.application.result;

import java.time.ZonedDateTime;

public record OrganizationMemberDto(
        Long id,
        Long userId,
        Long organizationId,
        String role,
        String status,
        ZonedDateTime joinedAt
) {}
