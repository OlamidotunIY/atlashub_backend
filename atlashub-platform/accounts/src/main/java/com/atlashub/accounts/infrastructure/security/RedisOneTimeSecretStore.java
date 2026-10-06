package com.atlashub.accounts.infrastructure.security;

import com.atlashub.shared.application.port.OneTimeSecretStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Component
public class RedisOneTimeSecretStore implements OneTimeSecretStore {
    private static final String KEY_PREFIX = "registration:credential:";

    private final StringRedisTemplate redisTemplate;

    public RedisOneTimeSecretStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String store(String secret, Duration timeToLive) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("Secret is required");
        }
        String reference = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(KEY_PREFIX + reference, secret, timeToLive);
        return reference;
    }

    @Override
    public Optional<String> claim(String reference) {
        if (reference == null || reference.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(redisTemplate.opsForValue().getAndDelete(KEY_PREFIX + reference));
    }
}
