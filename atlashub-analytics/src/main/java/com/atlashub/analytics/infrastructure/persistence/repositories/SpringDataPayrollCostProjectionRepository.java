package com.atlashub.analytics.infrastructure.persistence.repositories;

import com.atlashub.analytics.infrastructure.persistence.entities.PayrollCostProjectionJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataPayrollCostProjectionRepository extends JpaRepository<PayrollCostProjectionJpa, Long> {

    Optional<PayrollCostProjectionJpa> findByOrganizationIdAndEnvironmentAndMetricMonth(
            Long organizationId,
            String environment,
            String metricMonth
    );

    @Query("SELECT p FROM PayrollCostProjectionJpa p WHERE p.organizationId = :orgId AND p.environment = :env " +
           "AND p.metricMonth LIKE CONCAT(:year, '-%') ORDER BY p.metricMonth ASC")
    List<PayrollCostProjectionJpa> findByYear(
            @Param("orgId") Long orgId,
            @Param("env") String env,
            @Param("year") String year
    );
}
