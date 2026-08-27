package com.atlashub.auth.application.result;

import com.atlashub.auth.domain.valueobject.SessionStatus;
import java.time.ZonedDateTime;

public record SessionDto(
    Long id,
    String token,
    String ipAddress,
    String userAgent,
    SessionStatus status,
    ZonedDateTime createdAt,
    ZonedDateTime expiresAt,
    ZonedDateTime revokedAt
) {}
