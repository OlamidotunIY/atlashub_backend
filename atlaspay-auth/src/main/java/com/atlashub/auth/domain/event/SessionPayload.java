package com.atlashub.auth.domain.event;

import java.time.ZonedDateTime;

public record SessionPayload(
    String token,
    ZonedDateTime expiresAt
) {}
