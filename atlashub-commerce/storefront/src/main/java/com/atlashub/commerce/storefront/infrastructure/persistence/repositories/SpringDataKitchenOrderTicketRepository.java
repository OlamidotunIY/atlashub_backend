package com.atlashub.commerce.storefront.infrastructure.persistence.repositories;

import com.atlashub.commerce.storefront.domain.valueobject.KotStatus;
import com.atlashub.commerce.storefront.infrastructure.persistence.entities.KitchenOrderTicketJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataKitchenOrderTicketRepository extends JpaRepository<KitchenOrderTicketJpa, Long> {

    List<KitchenOrderTicketJpa> findByOutletIdAndStatus(Long outletId, KotStatus status);

    List<KitchenOrderTicketJpa> findBySalesOrderId(Long salesOrderId);
}
