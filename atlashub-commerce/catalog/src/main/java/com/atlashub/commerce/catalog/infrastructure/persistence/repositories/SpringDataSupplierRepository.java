package com.atlashub.commerce.catalog.infrastructure.persistence.repositories;

import com.atlashub.commerce.catalog.domain.valueobject.SupplierStatus;
import com.atlashub.commerce.catalog.infrastructure.persistence.entities.SupplierJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataSupplierRepository extends JpaRepository<SupplierJpa, Long> {

    List<SupplierJpa> findByOrganizationId(Long organizationId);

    List<SupplierJpa> findByOrganizationIdAndStatus(Long organizationId, SupplierStatus status);
}
