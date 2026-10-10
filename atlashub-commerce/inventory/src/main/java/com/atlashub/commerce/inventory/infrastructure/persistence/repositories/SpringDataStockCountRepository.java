package com.atlashub.commerce.inventory.infrastructure.persistence.repositories;

import com.atlashub.commerce.inventory.domain.valueobject.StockCountStatus;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.StockCountJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataStockCountRepository extends JpaRepository<StockCountJpa, Long> {

    List<StockCountJpa> findByOrganizationIdAndOutletIdAndStatus(Long organizationId, Long outletId, StockCountStatus status);
}
