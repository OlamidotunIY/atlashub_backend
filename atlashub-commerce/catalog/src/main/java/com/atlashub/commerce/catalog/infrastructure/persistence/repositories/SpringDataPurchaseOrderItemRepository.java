package com.atlashub.commerce.catalog.infrastructure.persistence.repositories;

import com.atlashub.commerce.catalog.infrastructure.persistence.entities.PurchaseOrderItemJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataPurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItemJpa, Long> {

    List<PurchaseOrderItemJpa> findByPurchaseOrderId(Long purchaseOrderId);

    List<PurchaseOrderItemJpa> findByPurchaseOrderIdIn(List<Long> purchaseOrderIds);

    void deleteByPurchaseOrderId(Long purchaseOrderId);
}
