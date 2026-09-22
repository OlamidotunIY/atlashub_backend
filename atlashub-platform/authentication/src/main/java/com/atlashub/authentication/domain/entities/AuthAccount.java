package com.atlashub.authentication.domain.entities;

import com.atlashub.authentication.domain.events.AuthAccountLocked;
import com.atlashub.authentication.domain.exceptions.AuthInvaraintError;
import com.atlashub.authentication.domain.exceptions.AuthLocked;
import com.atlashub.authentication.domain.exceptions.EmailAlreadyVerified;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Getter
public class AuthAccount extends AggregateRoot<Long> {
    private final Long id;
    private final Long userId;
    private final String email;
    private String passwordHash;
    private Boolean emailVerified;
    private int failedLoginAttempts;
    private ZonedDateTime lockedUntil;
    private ZonedDateTime lastLoginAt;
    private String lastLoginIp;
    private final ZonedDateTime createdAt;

    public AuthAccount(Long id, Long userId, String email, String passwordHash, Boolean emailVerified, int failedLoginAttempts, ZonedDateTime lockedUntil, ZonedDateTime lastLoginAt, String lastLoginIp, ZonedDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.emailVerified = emailVerified;
        this.failedLoginAttempts = failedLoginAttempts;
        this.lockedUntil = lockedUntil;
        this.lastLoginAt = lastLoginAt;
        this.lastLoginIp = lastLoginIp;
        this.createdAt = createdAt;
    }

    public static AuthAccount create(Long id, Long userId, String email, String passwordHash) {
        if (id == null || userId == null) {
            throw new AuthInvaraintError("id and userId field cannot be null");
        }

        return new AuthAccount(id, userId, email, passwordHash, false, 0, null, null, null, ZonedDateTime.now());
    }

    public void verifyEmail() {
        if (emailVerified) {
            throw new EmailAlreadyVerified();
        }

        this.emailVerified = true;
    }

    public void recordFailedLogin() {
        if (isLocked()) {
            throw new AuthLocked(lockedUntil.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        }
        failedLoginAttempts++;
        if (failedLoginAttempts >= 5) {
            this.lockedUntil = ZonedDateTime.now().plusMinutes(30);

            this.registerEvent(new AuthAccountLocked(UUID.randomUUID().toString(), this.id, ZonedDateTime.now(), CorrelationId.getOrCreate(), new AuthAccountLocked.Payload(this.lockedUntil)));
        }
    }

    public void recordSuccessfulLogin(String ip) {
        this.failedLoginAttempts = 0;
        this.lastLoginIp = ip;
        this.lastLoginAt = ZonedDateTime.now();
    }

    public void resetPassword(String newPasswordHash) {
        if (newPasswordHash == null) {
            throw new AuthInvaraintError("Your new password cannot be empty");
        }

        this.passwordHash = newPasswordHash;
    }

    public boolean isLocked() {
        return lockedUntil != null && ZonedDateTime.now().isBefore(lockedUntil);
    }

    public void unlock() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    @Override
    public Long getId() {
        return id;
    }
}
