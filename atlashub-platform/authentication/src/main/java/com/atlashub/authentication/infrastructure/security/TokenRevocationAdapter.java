package com.atlashub.authentication.infrastructure.security;

import com.atlashub.authentication.application.port.TokenRevocationPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class TokenRevocationAdapter implements TokenRevocationPort {

    private final StringRedisTemplate redisTemplate;
    private static final String PREFIX = "revoked:jti:";

    public TokenRevocationAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void revokeAccessToken(String jti, Duration remainingLifetime) {
        if (remainingLifetime.isNegative() || remainingLifetime.isZero()) {
            return;
        }

        String key = PREFIX + jti;

        redisTemplate.opsForValue().set(key, "true", remainingLifetime.getSeconds(), TimeUnit.SECONDS);
    }

    @Override
    public boolean isRevoked(String jti) {
        String key = PREFIX + jti;

        return redisTemplate.hasKey(key);
    }
}
