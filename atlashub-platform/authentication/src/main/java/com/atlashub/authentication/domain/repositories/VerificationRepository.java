package com.atlashub.authentication.domain.repositories;

import com.atlashub.authentication.domain.entities.Verification;
import com.atlashub.authentication.domain.valueobject.VerificationStatus;
import com.atlashub.authentication.domain.valueobject.VerificationType;
import com.atlashub.shared.domain.repository.Repository;

import java.util.Optional;

public interface VerificationRepository extends Repository<Verification> {
    Optional<Verification> findByIdentifierAndTypeAndStatus(
            String identifier, VerificationType type, VerificationStatus status);
}
