package com.atlashub.commerce.inventory.domain.repositories;

import com.atlashub.commerce.inventory.domain.entities.StockCount;
import com.atlashub.commerce.inventory.domain.valueobject.StockCountStatus;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;

public interface StockCountRepository extends Repository<StockCount> {

    List<StockCount> findByOrganizationIdAndOutletIdAndStatus(Long orgId, Long outletId, StockCountStatus status);
}
