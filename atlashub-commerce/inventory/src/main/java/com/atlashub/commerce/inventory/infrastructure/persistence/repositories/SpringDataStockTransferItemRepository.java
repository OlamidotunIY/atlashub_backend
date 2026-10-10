package com.atlashub.commerce.inventory.infrastructure.persistence.repositories;

import com.atlashub.commerce.inventory.infrastructure.persistence.entities.StockTransferItemJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataStockTransferItemRepository extends JpaRepository<StockTransferItemJpa, Long> {

    List<StockTransferItemJpa> findByTransferId(Long transferId);

    List<StockTransferItemJpa> findByTransferIdIn(List<Long> transferIds);

    void deleteByTransferId(Long transferId);
}
