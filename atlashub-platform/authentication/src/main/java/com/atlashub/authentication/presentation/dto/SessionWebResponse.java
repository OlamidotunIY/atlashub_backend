package com.atlashub.authentication.presentation.dto;

import com.atlashub.shared.application.security.ApiEnvironment;

import java.time.ZonedDateTime;

public record SessionWebResponse(
        Long id,
        Long organizationId,
        ApiEnvironment environment,
        ZonedDateTime expiresAt,
        String ipAddress,
        String userAgent
) {
}
