package com.atlashub.analytics.domain.repositories;

import com.atlashub.analytics.domain.entities.CustomerPerformanceProjection;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface CustomerPerformanceProjectionRepository extends Repository<CustomerPerformanceProjection> {

    Optional<CustomerPerformanceProjection> findByOrgEnvCustomerAndMonth(
            Long organizationId,
            String environment,
            Long customerId,
            String month
    );

    List<CustomerPerformanceProjection> findByOrgEnvAndMonth(
            Long organizationId,
            String environment,
            String month
    );
}
