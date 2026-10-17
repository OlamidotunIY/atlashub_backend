package com.atlashub.analytics.domain.repositories;

import com.atlashub.analytics.domain.entities.FinancingBehaviourProjection;
import com.atlashub.shared.domain.repository.Repository;

import java.util.Optional;

public interface FinancingBehaviourProjectionRepository extends Repository<FinancingBehaviourProjection> {

    Optional<FinancingBehaviourProjection> findByOrgEnvAndContextKey(
            Long organizationId,
            String environment,
            String paymentContextKey
    );
}
