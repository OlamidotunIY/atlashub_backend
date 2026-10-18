package com.atlashub.analytics.infrastructure.persistence.repositories;

import com.atlashub.analytics.infrastructure.persistence.entities.CustomerPerformanceProjectionJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataCustomerPerformanceProjectionRepository extends JpaRepository<CustomerPerformanceProjectionJpa, Long> {

    Optional<CustomerPerformanceProjectionJpa> findByOrganizationIdAndEnvironmentAndCustomerIdAndMetricMonth(
            Long organizationId,
            String environment,
            Long customerId,
            String metricMonth
    );

    @Query("SELECT c FROM CustomerPerformanceProjectionJpa c WHERE c.organizationId = :orgId AND c.environment = :env " +
           "AND c.metricMonth = :month")
    List<CustomerPerformanceProjectionJpa> findByMonth(
            @Param("orgId") Long orgId,
            @Param("env") String env,
            @Param("month") String month
    );
}
