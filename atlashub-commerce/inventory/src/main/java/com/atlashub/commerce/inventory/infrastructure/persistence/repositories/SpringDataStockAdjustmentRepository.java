package com.atlashub.commerce.inventory.infrastructure.persistence.repositories;

import com.atlashub.commerce.inventory.infrastructure.persistence.entities.StockAdjustmentJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataStockAdjustmentRepository extends JpaRepository<StockAdjustmentJpa, Long> {

    List<StockAdjustmentJpa> findByOrganizationIdAndInventoryId(Long organizationId, Long inventoryId);
}
