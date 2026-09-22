package com.atlashub.authentication.domain.repositories;

import com.atlashub.authentication.domain.entities.OtpVerification;
import com.atlashub.authentication.domain.valueobject.OtpStatus;
import com.atlashub.authentication.domain.valueobject.OtpType;
import com.atlashub.shared.domain.repository.Repository;

import java.util.Optional;

public interface OtpVerificationRepository extends Repository<OtpVerification> {
    Optional<OtpVerification> findByAuthAccountIdAndTypeAndStatus(Long authAccountId, OtpType type, OtpStatus status);
}
