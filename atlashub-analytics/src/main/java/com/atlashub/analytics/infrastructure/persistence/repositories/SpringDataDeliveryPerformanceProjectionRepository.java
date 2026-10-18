package com.atlashub.analytics.infrastructure.persistence.repositories;

import com.atlashub.analytics.infrastructure.persistence.entities.DeliveryPerformanceProjectionJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataDeliveryPerformanceProjectionRepository extends JpaRepository<DeliveryPerformanceProjectionJpa, Long> {

    Optional<DeliveryPerformanceProjectionJpa> findByOrganizationIdAndEnvironmentAndMetricMonth(
            Long organizationId,
            String environment,
            String metricMonth
    );
}
