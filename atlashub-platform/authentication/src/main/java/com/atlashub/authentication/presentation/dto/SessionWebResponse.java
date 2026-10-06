package com.atlashub.authentication.presentation.dto;

import java.time.ZonedDateTime;

public record SessionWebResponse(
        Long id,
        Long organizationId,
        String environment,
        ZonedDateTime expiresAt,
        String ipAddress,
        String userAgent
) {
}
