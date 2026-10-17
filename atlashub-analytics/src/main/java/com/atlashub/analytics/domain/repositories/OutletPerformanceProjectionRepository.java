package com.atlashub.analytics.domain.repositories;

import com.atlashub.analytics.domain.entities.OutletPerformanceProjection;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface OutletPerformanceProjectionRepository extends Repository<OutletPerformanceProjection> {

    Optional<OutletPerformanceProjection> findByOrgEnvOutletAndMonth(
            Long organizationId,
            String environment,
            Long outletId,
            String month
    );

    List<OutletPerformanceProjection> findByOrgEnvAndMonth(
            Long organizationId,
            String environment,
            String month
    );
}
