package com.atlashub.authentication.application.port;

import java.time.ZonedDateTime;
import java.util.Set;

public interface TokenPort {

    AccessTokenResult generateAccessToken(AccessTokenPayload payload);

    record AccessTokenPayload(
            String userId,
            String sessionId,
            String orgId,
            Set<String> permissions
    ) {
    }

    record AccessTokenResult(
            String token,
            String jti,
            ZonedDateTime expiresAt
    ) {
    }
}
