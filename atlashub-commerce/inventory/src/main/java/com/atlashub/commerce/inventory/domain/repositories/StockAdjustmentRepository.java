package com.atlashub.commerce.inventory.domain.repositories;

import com.atlashub.commerce.inventory.domain.entities.StockAdjustment;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;

public interface StockAdjustmentRepository extends Repository<StockAdjustment> {

    List<StockAdjustment> findByOrganizationIdAndInventoryId(Long orgId, Long inventoryId);
}
