package com.atlashub.analytics.infrastructure.persistence.repositories;

import com.atlashub.analytics.infrastructure.persistence.entities.OutletPerformanceProjectionJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataOutletPerformanceProjectionRepository extends JpaRepository<OutletPerformanceProjectionJpa, Long> {

    Optional<OutletPerformanceProjectionJpa> findByOrganizationIdAndEnvironmentAndOutletIdAndMetricMonth(
            Long organizationId,
            String environment,
            Long outletId,
            String metricMonth
    );

    @Query("SELECT o FROM OutletPerformanceProjectionJpa o WHERE o.organizationId = :orgId AND o.environment = :env " +
           "AND o.metricMonth = :month")
    List<OutletPerformanceProjectionJpa> findByMonth(
            @Param("orgId") Long orgId,
            @Param("env") String env,
            @Param("month") String month
    );
}
