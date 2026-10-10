package com.atlashub.pay.charges.infrastructure.persistence.repositories;

import com.atlashub.pay.charges.infrastructure.persistence.entities.ChargeJpa;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface SpringDataChargeRepository extends JpaRepository<ChargeJpa, Long> {
    Optional<ChargeJpa> findByOrganizationIdAndEnvironmentAndReference(Long organizationId, ApiEnvironment environment,
                                                                       String reference);

    Optional<ChargeJpa> findByProviderReferenceAndEnvironment(String providerReference, ApiEnvironment environment);

    @Query("SELECT c FROM ChargeJpa c WHERE c.status = com.atlashub.pay.charges.domain.valueobject.ChargeStatus.PENDING " +
           "AND (CAST(:createdBefore AS timestamp) IS NULL OR c.createdAt <= :createdBefore)")
    List<ChargeJpa> findPendingCharges(@Param("createdBefore") ZonedDateTime createdBefore);
}
