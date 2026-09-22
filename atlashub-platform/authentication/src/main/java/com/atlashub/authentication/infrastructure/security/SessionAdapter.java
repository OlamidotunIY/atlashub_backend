package com.atlashub.authentication.infrastructure.security;

import com.atlashub.authentication.application.port.SessionPort;
import com.atlashub.authentication.domain.valueobject.Session;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class SessionAdapter implements SessionPort {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public SessionAdapter(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(Session token) {
        try {
            String key = "session:" + token.refreshTokenHash();
            String userSetKey = "user:" + token.authAccountId() + ":sessions";

            String json = objectMapper.writeValueAsString(token);

            long ttlSeconds = Duration.between(ZonedDateTime.now(), token.refreshTokenExpiresAt()).getSeconds();

            if (ttlSeconds <= 0) return;

            redisTemplate.opsForValue().set(key, json, ttlSeconds, TimeUnit.SECONDS);
            redisTemplate.opsForSet().add(userSetKey, token.refreshTokenHash());
            redisTemplate.expire(userSetKey, ttlSeconds, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize session", e);
        }
    }

    @Override
    public Optional<Session> findByTokenHash(String tokenHash) {
        try {
            String json = redisTemplate.opsForValue().get("session:" + tokenHash);
            if (json == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, Session.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize session", e);
        }
    }

    @Override
    public void delete(String tokenHash) {
        findByTokenHash(tokenHash).ifPresent(session -> {
            String userSetKey = "user:" + session.authAccountId() + ":sessions";
            redisTemplate.opsForSet().remove(userSetKey, tokenHash);
            redisTemplate.delete("session:" + tokenHash);
        });
    }

    @Override
    public Set<Session> findAllByAuthAccountId(Long authAccountId) {
        String userSetKey = "user:" + authAccountId + ":sessions";
        Set<String> tokenHashes = redisTemplate.opsForSet().members(userSetKey);
        Set<Session> sessions = new HashSet<>();
        if (tokenHashes == null) return sessions;

        for (String hash : tokenHashes) {
            findByTokenHash(hash).ifPresent(sessions::add);
        }
        return sessions;
    }

    @Override
    public void deleteAllForUser(Long authAccountId) {
        String userSetKey = "user:" + authAccountId + ":sessions";
        Set<String> tokenHashes = redisTemplate.opsForSet().members(userSetKey);
        if (tokenHashes != null && !tokenHashes.isEmpty()) {
            for (String hash : tokenHashes) {
                redisTemplate.delete("session:" + hash);
            }
        }
        redisTemplate.delete(userSetKey);
    }
}
