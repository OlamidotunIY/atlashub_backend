package com.atlashub.authentication.application.port;

import java.time.ZonedDateTime;
import java.util.Set;

public interface TokenPort {

    AccessTokenResult generateAccessToken(AccessTokenPayload payload);

    IdTokenResult generateIdToken(IdTokenPayload payload);

    record AccessTokenPayload(
            String userId,
            String sessionId,
            String orgId,
            String environment,
            Set<String> permissions
    ) {
    }

    record AccessTokenResult(
            String token,
            String jti,
            ZonedDateTime expiresAt
    ) {
    }

    record IdTokenPayload(
            String userId,
            String clientId,
            String nonce,
            String email,
            boolean emailVerified,
            String firstName,
            String lastName,
            String orgId
    ) {
    }

    record IdTokenResult(
            String token,
            ZonedDateTime expiresAt
    ) {
    }
}
