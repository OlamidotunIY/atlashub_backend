package com.atlashub.authentication.domain.entities;

import com.atlashub.authentication.domain.events.OtpVerificationCreated;
import com.atlashub.authentication.domain.exceptions.AuthInvaraintError;
import com.atlashub.authentication.domain.exceptions.VerifyTokenError;
import com.atlashub.authentication.domain.valueobject.OtpStatus;
import com.atlashub.authentication.domain.valueobject.OtpType;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.UUID;

@Getter
public class OtpVerification extends AggregateRoot<Long> {
    private final Long id;
    private final Long authAccountId;
    private final String codeHash;
    private final OtpType type;
    private OtpStatus status;
    private ZonedDateTime expiresAt;
    private final ZonedDateTime createdAt;

    public OtpVerification(Long id, Long authAccountId, String codeHash, OtpType type, OtpStatus status, ZonedDateTime expiresAt, ZonedDateTime createdAt) {
        this.id = id;
        this.authAccountId = authAccountId;
        this.codeHash = codeHash;
        this.type = type;
        this.status = status;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public static OtpVerification create(Long id, Long authAccountId, String hashCode, OtpType type, ZonedDateTime expiresAt) {
        if (id == null || authAccountId == null || hashCode.isEmpty() || type == null || expiresAt == null) {
            throw new AuthInvaraintError("Missing Required fields");
        }

        ZonedDateTime now = ZonedDateTime.now();

        OtpVerification otpVerification = new OtpVerification(id, authAccountId, hashCode, type, OtpStatus.PENDING, expiresAt, now);

        otpVerification.registerEvent(new OtpVerificationCreated(UUID.randomUUID().toString(), otpVerification.id.toString(), now, CorrelationId.getOrCreate(), new OtpVerificationCreated.Payload(type, expiresAt)));

        return otpVerification;
    }

    void verifyToken() {
        if (!EnumSet.of(OtpStatus.USED, OtpStatus.REVOKED).contains(status)) {
            throw new VerifyTokenError();
        }

        this.status = OtpStatus.USED;
    }

    @Override
    public Long getId() {
        return id;
    }
}
