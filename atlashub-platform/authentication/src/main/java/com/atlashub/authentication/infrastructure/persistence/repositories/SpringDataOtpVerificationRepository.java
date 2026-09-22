package com.atlashub.authentication.infrastructure.persistence.repositories;

import com.atlashub.authentication.domain.valueobject.OtpStatus;
import com.atlashub.authentication.domain.valueobject.OtpType;
import com.atlashub.authentication.infrastructure.persistence.entities.OtpVerificationJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataOtpVerificationRepository extends JpaRepository<OtpVerificationJpa, Long> {
    Optional<OtpVerificationJpa> findByAuthAccountIdAndTypeAndStatus(
            Long authAccountId, OtpType type, OtpStatus status);
}
