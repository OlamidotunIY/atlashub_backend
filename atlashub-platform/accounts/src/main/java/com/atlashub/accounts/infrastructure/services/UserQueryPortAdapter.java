package com.atlashub.accounts.infrastructure.services;

import com.atlashub.accounts.infrastructure.persistence.entities.UserJPA;
import com.atlashub.accounts.infrastructure.persistence.repositories.SpringDataUserRepository;
import com.atlashub.shared.application.port.UserQueryPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class UserQueryPortAdapter implements UserQueryPort {

    public static final String USER_BY_ID_KEY_PREFIX = "cache:user:id:";
    public static final String USER_BY_EMAIL_KEY_PREFIX = "cache:user:email:";
    public static final String USER_ACTIVE_ORG_KEY_PREFIX = "cache:user:active-org:";

    private final SpringDataUserRepository springDataRepo;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public UserQueryPortAdapter(SpringDataUserRepository springDataRepo,
                                StringRedisTemplate redisTemplate,
                                ObjectMapper objectMapper,
                                @Value("${atlashub.cache.user-ttl:PT12H}") Duration ttl) {
        this.springDataRepo = springDataRepo;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.ttl = ttl;
    }

    @Override
    public Optional<UserDto> findById(Long userId) {
        String key = USER_BY_ID_KEY_PREFIX + userId;
        Optional<UserDto> cached = readUser(key);
        if (cached.isPresent()) {
            return cached;
        }

        Optional<UserDto> user = springDataRepo.findById(userId).map(this::toDto);
        user.ifPresent(this::cacheUser);
        return user;
    }

    @Override
    public Optional<UserDto> findByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        String key = USER_BY_EMAIL_KEY_PREFIX + normalizedEmail;
        Optional<UserDto> cached = readUser(key);
        if (cached.isPresent()) {
            return cached;
        }

        Optional<UserDto> user = springDataRepo.findByEmail(email).map(this::toDto);
        user.ifPresent(this::cacheUser);
        return user;
    }

    @Override
    public boolean existsById(Long userId) {
        try {
            if (redisTemplate.hasKey(USER_BY_ID_KEY_PREFIX + userId)) {
                return true;
            }
        } catch (RuntimeException ignored) {
        }
        return springDataRepo.existsById(userId);
    }

    @Override
    public Optional<Long> getActiveOrganizationId(Long userId) {
        String key = USER_ACTIVE_ORG_KEY_PREFIX + userId;
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return Optional.of(Long.valueOf(cached));
            }
        } catch (RuntimeException ignored) {
        }

        return findById(userId)
                .map(UserDto::activeOrganizationId)
                .map(activeOrgId -> {
                    cacheActiveOrganizationId(userId, activeOrgId);
                    return activeOrgId;
                });
    }

    private Optional<UserDto> readUser(String key) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) {
                return Optional.empty();
            }

            return Optional.of(objectMapper.readValue(json, UserDto.class));
        } catch (JsonProcessingException e) {
            redisTemplate.delete(key);
            return Optional.empty();
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private void cacheUser(UserDto user) {
        try {
            String json = objectMapper.writeValueAsString(user);
            long ttlSeconds = ttl.toSeconds();
            redisTemplate.opsForValue().set(USER_BY_ID_KEY_PREFIX + user.id(), json, ttlSeconds, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(USER_BY_EMAIL_KEY_PREFIX + normalizeEmail(user.email()), json, ttlSeconds, TimeUnit.SECONDS);
            cacheActiveOrganizationId(user.id(), user.activeOrganizationId());
        } catch (JsonProcessingException | RuntimeException ignored) {
        }
    }

    private void cacheActiveOrganizationId(Long userId, Long activeOrgId) {
        try {
            if (activeOrgId == null) {
                redisTemplate.delete(USER_ACTIVE_ORG_KEY_PREFIX + userId);
                return;
            }
            redisTemplate.opsForValue().set(USER_ACTIVE_ORG_KEY_PREFIX + userId, activeOrgId.toString(), ttl.toSeconds(), TimeUnit.SECONDS);
        } catch (RuntimeException ignored) {
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private UserDto toDto(UserJPA jpa) {
        return new UserDto(
                jpa.getId(),
                jpa.getFirstName(),
                jpa.getLastName(),
                jpa.getEmail(),
                jpa.getCountry(),
                jpa.getActiveOrganizationId(),
                jpa.isEmailVerified()
        );
    }
}
