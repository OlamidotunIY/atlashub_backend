package com.atlashub.commerce.catalog.domain.repositories;

import com.atlashub.commerce.catalog.domain.entities.Supplier;
import com.atlashub.commerce.catalog.domain.valueobject.SupplierStatus;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;

public interface SupplierRepository extends Repository<Supplier> {

    List<Supplier> findByOrganizationId(Long organizationId);

    List<Supplier> findByOrganizationIdAndStatus(Long organizationId, SupplierStatus status);
}
