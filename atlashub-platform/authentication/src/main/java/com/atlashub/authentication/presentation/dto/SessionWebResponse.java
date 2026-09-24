package com.atlashub.authentication.presentation.dto;

import java.time.ZonedDateTime;

public record SessionWebResponse(
        String token,
        ZonedDateTime expiresAt,
        String ipAddress,
        String userAgent
) {
}
