package com.atlashub.commerce.inventory.infrastructure.persistence.repositories;

import com.atlashub.commerce.inventory.infrastructure.persistence.entities.InventoryJpa;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataInventoryRepository extends JpaRepository<InventoryJpa, Long> {

    Optional<InventoryJpa> findByOrganizationIdAndOutletIdAndProductIdAndVariantId(Long organizationId, Long outletId, Long productId, Long variantId);

    Optional<InventoryJpa> findByOrganizationIdAndOutletIdAndProductId(Long organizationId, Long outletId, Long productId);

    List<InventoryJpa> findByOrganizationIdAndOutletId(Long organizationId, Long outletId);

    @Query("SELECT i FROM InventoryJpa i WHERE i.organizationId = :organizationId " +
            "AND (:outletId IS NULL OR i.outletId = :outletId) " +
            "AND i.quantity <= i.reorderLevel")
    List<InventoryJpa> findLowStock(
            @Param("organizationId") Long organizationId,
            @Param("outletId") Long outletId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryJpa i WHERE i.id = :id")
    Optional<InventoryJpa> findByIdWithLock(@Param("id") Long id);
}
