package com.atlashub.commerce.catalog.infrastructure.persistence.repositories;

import com.atlashub.commerce.catalog.domain.valueobject.PurchaseOrderStatus;
import com.atlashub.commerce.catalog.infrastructure.persistence.entities.PurchaseOrderJpa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataPurchaseOrderRepository extends JpaRepository<PurchaseOrderJpa, Long> {

    Page<PurchaseOrderJpa> findByOrganizationId(Long organizationId, Pageable pageable);

    Page<PurchaseOrderJpa> findByOrganizationIdAndStatus(Long organizationId, PurchaseOrderStatus status, Pageable pageable);
}
