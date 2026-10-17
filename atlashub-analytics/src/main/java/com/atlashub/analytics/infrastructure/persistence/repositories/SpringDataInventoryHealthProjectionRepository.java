package com.atlashub.analytics.infrastructure.persistence.repositories;

import com.atlashub.analytics.infrastructure.persistence.entities.InventoryHealthProjectionJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataInventoryHealthProjectionRepository extends JpaRepository<InventoryHealthProjectionJpa, Long> {

    Optional<InventoryHealthProjectionJpa> findByOrganizationIdAndEnvironmentAndOutletIdAndProductId(
            Long organizationId,
            String environment,
            Long outletId,
            Long productId
    );

    @Query("SELECT i FROM InventoryHealthProjectionJpa i WHERE i.organizationId = :orgId AND i.environment = :env " +
           "AND (:outletId IS NULL OR i.outletId = :outletId)")
    List<InventoryHealthProjectionJpa> findByOutlet(
            @Param("orgId") Long orgId,
            @Param("env") String env,
            @Param("outletId") Long outletId
    );
}
