package com.atlashub.analytics.infrastructure.persistence.repositories;

import com.atlashub.analytics.infrastructure.persistence.entities.DailySalesProjectionJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SpringDataDailySalesProjectionRepository extends JpaRepository<DailySalesProjectionJpa, Long> {

    Optional<DailySalesProjectionJpa> findByOrganizationIdAndEnvironmentAndOutletIdAndMetricDate(
            Long organizationId,
            String environment,
            Long outletId,
            LocalDate metricDate
    );

    @Query("SELECT d FROM DailySalesProjectionJpa d WHERE d.organizationId = :orgId AND d.environment = :env " +
           "AND (:outletId IS NULL OR d.outletId = :outletId) " +
           "AND d.metricDate >= :from AND d.metricDate <= :to ORDER BY d.metricDate ASC")
    List<DailySalesProjectionJpa> findByDateRange(
            @Param("orgId") Long orgId,
            @Param("env") String env,
            @Param("outletId") Long outletId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}
