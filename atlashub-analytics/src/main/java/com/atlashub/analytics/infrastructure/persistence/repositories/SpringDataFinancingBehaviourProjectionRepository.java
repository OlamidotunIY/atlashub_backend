package com.atlashub.analytics.infrastructure.persistence.repositories;

import com.atlashub.analytics.infrastructure.persistence.entities.FinancingBehaviourProjectionJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataFinancingBehaviourProjectionRepository extends JpaRepository<FinancingBehaviourProjectionJpa, Long> {

    Optional<FinancingBehaviourProjectionJpa> findByOrganizationIdAndEnvironmentAndPaymentContextKey(
            Long organizationId,
            String environment,
            String paymentContextKey
    );
}
