package com.atlashub.commerce.inventory.domain.repositories;

import com.atlashub.commerce.inventory.domain.entities.Inventory;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends Repository<Inventory> {

    Optional<Inventory> findByOrganizationIdAndOutletIdAndProductIdAndVariantId(Long orgId, Long outletId, Long productId, Long variantId);

    Optional<Inventory> findByOrganizationIdAndOutletIdAndProductId(Long orgId, Long outletId, Long productId);

    List<Inventory> findByOrganizationIdAndOutletId(Long orgId, Long outletId);

    List<Inventory> findLowStock(Long orgId, Long outletId);
}
