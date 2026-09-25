package com.atlashub.authentication.infrastructure.persistence.repositories;

import com.atlashub.authentication.domain.valueobject.VerificationStatus;
import com.atlashub.authentication.domain.valueobject.VerificationType;
import com.atlashub.authentication.infrastructure.persistence.entities.VerificationJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataVerificationRepository extends JpaRepository<VerificationJpa, Long> {

    Optional<VerificationJpa> findByIdentifierAndVerificationTypeAndVerificationStatus(
            String identifier, VerificationType verificationType, VerificationStatus verificationStatus);
}
