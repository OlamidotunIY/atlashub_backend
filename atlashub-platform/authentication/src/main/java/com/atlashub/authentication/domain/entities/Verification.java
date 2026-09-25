package com.atlashub.authentication.domain.entities;

import com.atlashub.authentication.domain.events.OtpVerificationCreated;
import com.atlashub.authentication.domain.exceptions.AuthInvaraintError;
import com.atlashub.authentication.domain.exceptions.VerifyTokenError;
import com.atlashub.authentication.domain.valueobject.VerificationStatus;
import com.atlashub.authentication.domain.valueobject.VerificationType;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class Verification extends AggregateRoot<Long> {

    private final Long id;
    private final String identifier;          // user's email
    private final String valueHash;           // hashed OTP/code
    private final VerificationType verificationType;
    private VerificationStatus verificationStatus;
    private final ZonedDateTime expiresAt;
    private int attempts;
    private final int maxAttempts;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public Verification(Long id, String identifier, String valueHash, VerificationType verificationType,
                        VerificationStatus verificationStatus, ZonedDateTime expiresAt,
                        int attempts, int maxAttempts, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.identifier = identifier;
        this.valueHash = valueHash;
        this.verificationType = verificationType;
        this.verificationStatus = verificationStatus;
        this.expiresAt = expiresAt;
        this.attempts = attempts;
        this.maxAttempts = maxAttempts;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Verification create(Long id, String identifier, String valueHash,
                                      VerificationType verificationType, ZonedDateTime expiresAt) {
        if (id == null || identifier == null || identifier.isBlank() || valueHash == null
                || valueHash.isEmpty() || verificationType == null || expiresAt == null) {
            throw new AuthInvaraintError("Missing required fields for Verification");
        }

        ZonedDateTime now = ZonedDateTime.now();

        Verification verification = new Verification(
                id, identifier, valueHash, verificationType,
                VerificationStatus.pending, expiresAt, 0, 5, now, now);

        verification.registerEvent(new OtpVerificationCreated(
                UUID.randomUUID().toString(), id, now,
                CorrelationId.getOrCreate(),
                new OtpVerificationCreated.Payload(verificationType, expiresAt)));

        return verification;
    }

    /**
     * Marks this verification as used/verified. Throws if already verified, expired, or max attempts exceeded.
     */
    public void verify() {
        if (verificationStatus != VerificationStatus.pending) {
            throw new VerifyTokenError();
        }
        this.attempts++;
        this.verificationStatus = VerificationStatus.verified;
        this.updatedAt = ZonedDateTime.now();
    }

    /**
     * Records a failed verification attempt and sets status to max_attempts_exceeded if the limit is reached.
     */
    public void recordFailedAttempt() {
        this.attempts++;
        this.updatedAt = ZonedDateTime.now();
        if (this.attempts >= this.maxAttempts) {
            this.verificationStatus = VerificationStatus.max_attempts_exceeded;
        }
    }

    @Override
    public Long getId() {
        return id;
    }
}
