package com.atlashub.analytics.infrastructure.persistence.repositories;

import com.atlashub.analytics.infrastructure.persistence.entities.FinancialHealthProjectionJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface SpringDataFinancialHealthProjectionRepository extends JpaRepository<FinancialHealthProjectionJpa, Long> {

    Optional<FinancialHealthProjectionJpa> findByOrganizationIdAndEnvironmentAndPeriodEnd(
            Long organizationId,
            String environment,
            LocalDate periodEnd
    );

    @Query("SELECT f FROM FinancialHealthProjectionJpa f WHERE f.organizationId = :orgId AND f.environment = :env " +
           "ORDER BY f.periodEnd DESC LIMIT 1")
    Optional<FinancialHealthProjectionJpa> findLatest(
            @Param("orgId") Long orgId,
            @Param("env") String env
    );
}
