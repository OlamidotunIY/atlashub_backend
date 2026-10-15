package com.atlashub.accounting.ap.infrastructure.persistence.repositories;

import com.atlashub.accounting.ap.domain.valueobject.ApStatus;
import com.atlashub.accounting.ap.infrastructure.persistence.entities.SupplierInvoiceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataSupplierInvoiceRepository extends JpaRepository<SupplierInvoiceJpaEntity, Long> {

    Optional<SupplierInvoiceJpaEntity> findByOrganizationIdAndPurchaseOrderId(Long organizationId, Long purchaseOrderId);

    List<SupplierInvoiceJpaEntity> findByOrganizationIdAndSupplierId(Long organizationId, Long supplierId);

    List<SupplierInvoiceJpaEntity> findByOrganizationIdAndStatus(Long organizationId, ApStatus status);

    List<SupplierInvoiceJpaEntity> findByOrganizationId(Long organizationId);
}
