package com.atlashub.analytics.domain.repositories;

import com.atlashub.analytics.domain.entities.InventoryHealthProjection;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface InventoryHealthProjectionRepository extends Repository<InventoryHealthProjection> {

    Optional<InventoryHealthProjection> findByOrgEnvOutletAndProduct(
            Long organizationId,
            String environment,
            Long outletId,
            Long productId
    );

    List<InventoryHealthProjection> findByOrgEnvAndOutlet(
            Long organizationId,
            String environment,
            Long outletId
    );
}
