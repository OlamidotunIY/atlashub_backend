package com.atlashub.commerce.catalog.infrastructure.persistence.repositories;

import com.atlashub.commerce.catalog.domain.valueobject.VendorStatus;
import com.atlashub.commerce.catalog.infrastructure.persistence.entities.VendorJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataVendorRepository extends JpaRepository<VendorJpa, Long> {

    List<VendorJpa> findByOrganizationId(Long organizationId);

    List<VendorJpa> findByOrganizationIdAndStatus(Long organizationId, VendorStatus status);

    Optional<VendorJpa> findByOrganizationIdAndUserId(Long organizationId, Long userId);
}
