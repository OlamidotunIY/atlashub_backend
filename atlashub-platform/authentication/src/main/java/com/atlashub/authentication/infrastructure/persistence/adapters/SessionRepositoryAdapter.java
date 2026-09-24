package com.atlashub.authentication.infrastructure.persistence.adapters;

import com.atlashub.authentication.domain.entities.Session;
import com.atlashub.authentication.domain.repositories.SessionRepository;
import com.atlashub.authentication.infrastructure.persistence.entities.SessionJpa;
import com.atlashub.authentication.infrastructure.persistence.mappers.SessionMapper;
import com.atlashub.authentication.infrastructure.persistence.repositories.SpringDataSessionRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.application.service.HashingUtils;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Dual-write repository adapter for Session.
 * <p>
 * Write strategy: every {@link #save(Session)} writes to both DB (durable audit copy)
 * and Redis (primary read source, keyed by sha256 of the raw token).
 * <p>
 * Read strategy: exclusively from Redis. A missing Redis key means the session has expired.
 */
@Component
public class SessionRepositoryAdapter
        extends JpaBaseRepository<Session, SessionJpa>
        implements SessionRepository {

    private static final String SESSION_KEY_PREFIX = "session:";
    private static final String SESSION_ID_KEY_PREFIX = "session:id:";
    private static final String USER_SESSIONS_KEY_PREFIX = "user:";
    private static final String USER_SESSIONS_KEY_SUFFIX = ":sessions";

    private final SpringDataSessionRepository springDataRepo;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public SessionRepositoryAdapter(SpringDataSessionRepository springDataRepo,
                                    SessionMapper mapper,
                                    DomainSequenceGenerator sequenceGenerator,
                                    DomainEventPublisher eventPublisher,
                                    StringRedisTemplate redisTemplate,
                                    ObjectMapper objectMapper) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    protected String getSequenceName() {
        return "session_seq";
    }

    /**
     * Dual-write: saves to DB then to Redis (keyed by sha256 of token).
     * TTL in Redis = remaining seconds until session expiry.
     */
    @Override
    @Transactional
    public Session save(Session session) {
        // 1. Persist to DB
        Session saved = super.save(session);

        // 2. Write to Redis
        long ttlSeconds = Duration.between(ZonedDateTime.now(), session.getExpiresAt()).getSeconds();
        if (ttlSeconds > 0) {
            String tokenHash = HashingUtils.sha256Hex(session.getToken());
            String redisKey = SESSION_KEY_PREFIX + tokenHash;
            String sessionIdKey = SESSION_ID_KEY_PREFIX + session.getId();
            String userSetKey = USER_SESSIONS_KEY_PREFIX + session.getUserId() + USER_SESSIONS_KEY_SUFFIX;

            try {
                String json = objectMapper.writeValueAsString(session);
                redisTemplate.opsForValue().set(redisKey, json, ttlSeconds, TimeUnit.SECONDS);
                redisTemplate.opsForValue().set(sessionIdKey, json, ttlSeconds, TimeUnit.SECONDS);
                redisTemplate.opsForSet().add(userSetKey, tokenHash);
                redisTemplate.expire(userSetKey, ttlSeconds, TimeUnit.SECONDS);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize session to Redis", e);
            }
        }
        return saved;
    }

    /**
     * Reads exclusively from Redis. Returns empty if not found (session expired or revoked).
     */
    @Override
    public Optional<Session> findById(Long id) {
        try {
            String json = redisTemplate.opsForValue().get(SESSION_ID_KEY_PREFIX + id);
            if (json == null) return Optional.empty();
            return Optional.of(objectMapper.readValue(json, Session.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize session from Redis", e);
        }
    }

    @Override
    public boolean existsById(Long id) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(SESSION_ID_KEY_PREFIX + id));
    }

    /**
     * Reads exclusively from Redis. Returns empty if not found (session expired).
     */
    @Override
    public Optional<Session> findByToken(String token) {
        String tokenHash = HashingUtils.sha256Hex(token);
        try {
            String json = redisTemplate.opsForValue().get(SESSION_KEY_PREFIX + tokenHash);
            if (json == null) return Optional.empty();
            return Optional.of(objectMapper.readValue(json, Session.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize session from Redis", e);
        }
    }

    /**
     * Reads all active sessions for a user from Redis. Redis-only.
     */
    @Override
    public Set<Session> findAllByUserId(String userId) {
        String userSetKey = USER_SESSIONS_KEY_PREFIX + userId + USER_SESSIONS_KEY_SUFFIX;
        Set<String> tokenHashes = redisTemplate.opsForSet().members(userSetKey);
        if (tokenHashes == null) return new HashSet<>();

        return tokenHashes.stream()
                .map(hash -> {
                    try {
                        String json = redisTemplate.opsForValue().get(SESSION_KEY_PREFIX + hash);
                        if (json == null) return Optional.<Session>empty();
                        return Optional.of(objectMapper.readValue(json, Session.class));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to deserialize session from Redis", e);
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    /**
     * Deletes from both Redis and DB. Resolves DB ID via Redis lookup.
     */
    @Override
    @Transactional
    public void deleteByToken(String token) {
        String tokenHash = HashingUtils.sha256Hex(token);
        findByToken(token).ifPresent(session -> {
            String userSetKey = USER_SESSIONS_KEY_PREFIX + session.getUserId() + USER_SESSIONS_KEY_SUFFIX;
            redisTemplate.opsForSet().remove(userSetKey, tokenHash);
            redisTemplate.delete(SESSION_KEY_PREFIX + tokenHash);
            redisTemplate.delete(SESSION_ID_KEY_PREFIX + session.getId());
            springDataRepo.deleteById(session.getId());
        });
    }

    /**
     * Deletes all sessions for a user from both Redis and DB.
     */
    @Override
    @Transactional
    public void deleteAllByUserId(String userId) {
        String userSetKey = USER_SESSIONS_KEY_PREFIX + userId + USER_SESSIONS_KEY_SUFFIX;
        Set<String> tokenHashes = redisTemplate.opsForSet().members(userSetKey);

        if (tokenHashes != null) {
            for (String hash : tokenHashes) {
                String json = redisTemplate.opsForValue().get(SESSION_KEY_PREFIX + hash);
                if (json != null) {
                    try {
                        Session session = objectMapper.readValue(json, Session.class);
                        redisTemplate.delete(SESSION_ID_KEY_PREFIX + session.getId());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to deserialize session from Redis", e);
                    }
                }
                redisTemplate.delete(SESSION_KEY_PREFIX + hash);
            }
        }
        redisTemplate.delete(userSetKey);
        springDataRepo.deleteAllByUserId(userId);
    }
}
