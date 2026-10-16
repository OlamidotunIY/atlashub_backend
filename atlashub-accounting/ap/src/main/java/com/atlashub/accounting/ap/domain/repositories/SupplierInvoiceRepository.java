package com.atlashub.accounting.ap.domain.repositories;

import com.atlashub.accounting.ap.domain.entities.SupplierInvoice;
import com.atlashub.accounting.ap.domain.valueobject.ApStatus;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface SupplierInvoiceRepository extends Repository<SupplierInvoice> {

    Optional<SupplierInvoice> findByOrganizationIdAndPurchaseOrderId(Long organizationId, Long purchaseOrderId);

    List<SupplierInvoice> findByOrganizationIdAndSupplierId(Long organizationId, Long supplierId);

    List<SupplierInvoice> findByOrganizationIdAndStatus(Long organizationId, ApStatus status);

    List<SupplierInvoice> findByOrganizationId(Long organizationId);
}
