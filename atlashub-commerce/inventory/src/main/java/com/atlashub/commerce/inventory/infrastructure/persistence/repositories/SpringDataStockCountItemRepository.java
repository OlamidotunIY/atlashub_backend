package com.atlashub.commerce.inventory.infrastructure.persistence.repositories;

import com.atlashub.commerce.inventory.infrastructure.persistence.entities.StockCountItemJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataStockCountItemRepository extends JpaRepository<StockCountItemJpa, Long> {

    List<StockCountItemJpa> findByStockCountId(Long stockCountId);

    List<StockCountItemJpa> findByStockCountIdIn(List<Long> stockCountIds);

    void deleteByStockCountId(Long stockCountId);
}
