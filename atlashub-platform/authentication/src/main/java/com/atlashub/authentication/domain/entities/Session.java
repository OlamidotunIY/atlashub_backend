package com.atlashub.authentication.domain.entities;

import com.atlashub.authentication.domain.exceptions.AuthenticationInvariantException;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.application.security.ApiEnvironment;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class Session extends AggregateRoot<Long> {

    private final Long id;
    private ZonedDateTime expiresAt;
    private final String tokenHash;
    private final String userId;         // the authenticated user's ID (from accounts module)
    private final Long organizationId;
    private final String environment;
    private final String deviceFingerprint;
    private final String tokenFamilyId;
    private final String ipAddress;
    private final String userAgent;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    /** All-args constructor — used by MapStruct reconstitution. */
    public Session(Long id, ZonedDateTime expiresAt, String tokenHash, String userId,
                   Long organizationId, String environment, String deviceFingerprint, String tokenFamilyId,
                   String ipAddress, String userAgent,
                   ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.expiresAt = expiresAt;
        this.tokenHash = tokenHash;
        this.userId = userId;
        this.organizationId = organizationId;
        this.environment = ApiEnvironment.parse(environment).name();
        this.deviceFingerprint = deviceFingerprint;
        this.tokenFamilyId = tokenFamilyId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Session create(Long id, String tokenHash, String userId, Long organizationId,
                                 String environment, String deviceFingerprint, String tokenFamilyId,
                                 ZonedDateTime expiresAt, String ipAddress, String userAgent) {
        if (id == null) throw new AuthenticationInvariantException("Session id cannot be null");
        if (tokenHash == null || tokenHash.isBlank()) throw new AuthenticationInvariantException("Session token hash cannot be empty");
        if (userId == null || userId.isBlank()) throw new AuthenticationInvariantException("Session userId cannot be empty");
        if (organizationId == null) throw new AuthenticationInvariantException("Session organizationId cannot be null");
        if (environment == null || environment.isBlank()) throw new AuthenticationInvariantException("Session environment is required");
        if (deviceFingerprint == null || deviceFingerprint.isBlank()) throw new AuthenticationInvariantException("Device fingerprint is required");
        if (tokenFamilyId == null || tokenFamilyId.isBlank()) throw new AuthenticationInvariantException("Token family is required");
        if (expiresAt == null || !expiresAt.isAfter(ZonedDateTime.now())) throw new AuthenticationInvariantException("Session expiry must be in the future");

        ZonedDateTime now = ZonedDateTime.now();
        return new Session(id, expiresAt, tokenHash, userId, organizationId, environment,
                deviceFingerprint, tokenFamilyId, ipAddress, userAgent, now, now);
    }

    public boolean isExpired() {
        return ZonedDateTime.now().isAfter(expiresAt);
    }

    public void extend(ZonedDateTime newExpiresAt) {
        if (!newExpiresAt.isAfter(this.expiresAt)) {
            throw new AuthenticationInvariantException("New expiration date must be after the current one");
        }
        this.expiresAt = newExpiresAt;
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
