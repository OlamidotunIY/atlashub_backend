package com.atlashub.analytics.domain.repositories;

import com.atlashub.analytics.domain.entities.ProductDemandProjection;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface ProductDemandProjectionRepository extends Repository<ProductDemandProjection> {

    Optional<ProductDemandProjection> findByOrgEnvProductOutletAndMonth(
            Long organizationId,
            String environment,
            Long productId,
            Long outletId,
            String month
    );

    List<ProductDemandProjection> findTopProducts(Long organizationId, String environment, String month);
}
