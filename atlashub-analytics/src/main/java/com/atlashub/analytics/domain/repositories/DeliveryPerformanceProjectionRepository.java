package com.atlashub.analytics.domain.repositories;

import com.atlashub.analytics.domain.entities.DeliveryPerformanceProjection;
import com.atlashub.shared.domain.repository.Repository;

import java.util.Optional;

public interface DeliveryPerformanceProjectionRepository extends Repository<DeliveryPerformanceProjection> {

    Optional<DeliveryPerformanceProjection> findByOrgEnvAndMonth(Long organizationId, String environment, String metricMonth);
}
