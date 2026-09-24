package com.atlashub.authentication.domain.entities;

import com.atlashub.authentication.domain.exceptions.AuthInvaraintError;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class Session extends AggregateRoot<Long> {

    private final Long id;
    private ZonedDateTime expiresAt;
    private final String token;          // raw session token — stored in DB, hashed for Redis key
    private final String userId;         // the authenticated user's ID (from accounts module)
    private final String ipAddress;
    private final String userAgent;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    /** All-args constructor — used by MapStruct reconstitution. */
    public Session(Long id, ZonedDateTime expiresAt, String token, String userId,
                   String ipAddress, String userAgent,
                   ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.expiresAt = expiresAt;
        this.token = token;
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Session create(Long id, String token, String userId,
                                  ZonedDateTime expiresAt, String ipAddress, String userAgent) {
        if (id == null) throw new AuthInvaraintError("Session id cannot be null");
        if (token == null || token.isBlank()) throw new AuthInvaraintError("Session token cannot be empty");
        if (userId == null || userId.isBlank()) throw new AuthInvaraintError("Session userId cannot be empty");

        ZonedDateTime now = ZonedDateTime.now();
        return new Session(id, expiresAt, token, userId, ipAddress, userAgent, now, now);
    }

    public boolean isExpired() {
        return ZonedDateTime.now().isAfter(expiresAt);
    }

    public void extend(ZonedDateTime newExpiresAt) {
        if (!newExpiresAt.isAfter(this.expiresAt)) {
            throw new AuthInvaraintError("New expiration date must be after the current one");
        }
        this.expiresAt = newExpiresAt;
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
