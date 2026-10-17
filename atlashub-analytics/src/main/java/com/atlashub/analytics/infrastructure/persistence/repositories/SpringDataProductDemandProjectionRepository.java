package com.atlashub.analytics.infrastructure.persistence.repositories;

import com.atlashub.analytics.infrastructure.persistence.entities.ProductDemandProjectionJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataProductDemandProjectionRepository extends JpaRepository<ProductDemandProjectionJpa, Long> {

    Optional<ProductDemandProjectionJpa> findByOrganizationIdAndEnvironmentAndProductIdAndOutletIdAndMetricMonth(
            Long organizationId,
            String environment,
            Long productId,
            Long outletId,
            String metricMonth
    );

    @Query("SELECT p FROM ProductDemandProjectionJpa p WHERE p.organizationId = :orgId AND p.environment = :env " +
           "AND p.metricMonth = :month ORDER BY p.totalRevenue DESC")
    List<ProductDemandProjectionJpa> findTopProducts(
            @Param("orgId") Long orgId,
            @Param("env") String env,
            @Param("month") String month
    );
}
