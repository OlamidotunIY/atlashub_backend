package com.atlashub.analytics.domain.repositories;

import com.atlashub.analytics.domain.entities.FinancialHealthProjection;
import com.atlashub.shared.domain.repository.Repository;

import java.time.LocalDate;
import java.util.Optional;

public interface FinancialHealthProjectionRepository extends Repository<FinancialHealthProjection> {

    Optional<FinancialHealthProjection> findByOrgEnvAndPeriodEnd(Long organizationId, String environment, LocalDate periodEnd);

    Optional<FinancialHealthProjection> findLatestByOrgEnv(Long organizationId, String environment);
}
