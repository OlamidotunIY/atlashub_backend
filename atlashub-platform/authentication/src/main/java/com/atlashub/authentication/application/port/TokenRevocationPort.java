package com.atlashub.authentication.application.port;

import java.time.Duration;

public interface TokenRevocationPort {
    void revokeAccessToken(String jti, Duration remainingLifetime);
    boolean isRevoked(String jti);
}
