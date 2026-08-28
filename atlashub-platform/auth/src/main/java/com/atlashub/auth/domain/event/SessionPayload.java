package com.atlashub.auth.domain.event;

import com.atlashub.auth.domain.valueobject.PrincipalType;
import java.time.ZonedDateTime;

public record SessionPayload(
    String token,
    ZonedDateTime expiresAt,
    Long principalId,
    PrincipalType principalType,
    String ipAddress,
    String userAgent
) {}
