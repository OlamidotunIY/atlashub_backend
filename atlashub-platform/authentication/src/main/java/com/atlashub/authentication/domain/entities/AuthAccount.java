package com.atlashub.authentication.domain.entities;

import com.atlashub.authentication.domain.events.AuthAccountLocked;
import com.atlashub.authentication.domain.events.AuthEmailVerifiedEvent;
import com.atlashub.authentication.domain.exceptions.AuthInvaraintError;
import com.atlashub.authentication.domain.exceptions.AuthLocked;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Getter
public class AuthAccount extends AggregateRoot<Long> {

    private final Long id;
    private final String accountId;              // email for credential, provider sub-id for OAuth
    private final String providerId;             // "CREDENTIALS" or "google", etc.
    private final Long userId;
    private String accessToken;
    private String refreshToken;
    private String idToken;
    private ZonedDateTime accessTokenExpiresAt;
    private ZonedDateTime refreshTokenExpiresAt;
    private String scope;
    private String password;                     // BCrypt hash — null for OAuth accounts
    private int failedLoginAttempts;
    private ZonedDateTime lockedUntil;
    private ZonedDateTime lastLoginAt;
    private String lastLoginIp;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    /** All-args constructor used by MapStruct reconstitution. */
    public AuthAccount(Long id, String accountId, String providerId, Long userId,
                       String accessToken, String refreshToken, String idToken,
                       ZonedDateTime accessTokenExpiresAt, ZonedDateTime refreshTokenExpiresAt,
                       String scope, String password, int failedLoginAttempts,
                       ZonedDateTime lockedUntil, ZonedDateTime lastLoginAt, String lastLoginIp,
                       ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.accountId = accountId;
        this.providerId = providerId;
        this.userId = userId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.idToken = idToken;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
        this.scope = scope;
        this.password = password;
        this.failedLoginAttempts = failedLoginAttempts;
        this.lockedUntil = lockedUntil;
        this.lastLoginAt = lastLoginAt;
        this.lastLoginIp = lastLoginIp;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Creates a credential-based account (email + password). */
    public static AuthAccount createCredentialsAccount(Long id, Long userId, String email, String passwordHash) {
        if (id == null || userId == null) {
            throw new AuthInvaraintError("id and userId cannot be null");
        }
        ZonedDateTime now = ZonedDateTime.now();
        return new AuthAccount(
                id,
                email.trim().toLowerCase(),
                "CREDENTIALS",
                userId,
                null, null, null, null, null,
                "user",
                passwordHash,
                0, null, null, null,
                now, now
        );
    }

    /** Creates an OAuth account (Google, GitHub, etc.). */
    public static AuthAccount createOAuthAccount(Long id, Long userId, String accountId,
                                                  String providerId, String accessToken,
                                                  String refreshToken, String idToken,
                                                  ZonedDateTime accessTokenExpiresAt,
                                                  ZonedDateTime refreshTokenExpiresAt,
                                                  String scope) {
        if (id == null || userId == null) {
            throw new AuthInvaraintError("id and userId cannot be null");
        }
        ZonedDateTime now = ZonedDateTime.now();
        return new AuthAccount(
                id, accountId, providerId, userId,
                accessToken, refreshToken, idToken,
                accessTokenExpiresAt, refreshTokenExpiresAt,
                scope, null,
                0, null, null, null,
                now, now
        );
    }

    public void recordFailedLogin() {
        if (isLocked()) {
            throw new AuthLocked(lockedUntil.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        }
        failedLoginAttempts++;
        if (failedLoginAttempts >= 5) {
            this.lockedUntil = ZonedDateTime.now().plusMinutes(30);
            this.updatedAt = ZonedDateTime.now();
            this.registerEvent(new AuthAccountLocked(
                    UUID.randomUUID().toString(), this.id, ZonedDateTime.now(),
                    CorrelationId.getOrCreate(),
                    new AuthAccountLocked.Payload(this.lockedUntil)));
        }
    }

    public void recordSuccessfulLogin(String ip) {
        this.failedLoginAttempts = 0;
        this.lastLoginIp = ip;
        this.lastLoginAt = ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();
    }

    /**
     * Records that the email has been verified. Fires {@link AuthEmailVerifiedEvent} so
     * the accounts module User can update its own {@code emailVerified} flag.
     */
    public void recordEmailVerified() {
        this.updatedAt = ZonedDateTime.now();
        this.registerEvent(new AuthEmailVerifiedEvent(
                UUID.randomUUID().toString(), this.id, ZonedDateTime.now(),
                CorrelationId.getOrCreate(),
                new AuthEmailVerifiedEvent.Payload(this.userId.toString(), this.accountId)));
    }

    public void updatePassword(String newPasswordHash) {
        if (newPasswordHash == null || newPasswordHash.isBlank()) {
            throw new AuthInvaraintError("New password cannot be empty");
        }
        this.password = newPasswordHash;
        this.updatedAt = ZonedDateTime.now();
    }

    public void updateTokens(String accessToken, String refreshToken, String idToken,
                              ZonedDateTime accessTokenExpiresAt, ZonedDateTime refreshTokenExpiresAt) {
        this.accessToken = accessToken;
        if (refreshToken != null) this.refreshToken = refreshToken;
        if (idToken != null) this.idToken = idToken;
        if (accessTokenExpiresAt != null) this.accessTokenExpiresAt = accessTokenExpiresAt;
        if (refreshTokenExpiresAt != null) this.refreshTokenExpiresAt = refreshTokenExpiresAt;
        this.updatedAt = ZonedDateTime.now();
    }

    public boolean isLocked() {
        return lockedUntil != null && ZonedDateTime.now().isBefore(lockedUntil);
    }

    public void unlock() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
