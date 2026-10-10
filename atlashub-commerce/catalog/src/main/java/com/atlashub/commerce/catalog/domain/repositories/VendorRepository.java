package com.atlashub.commerce.catalog.domain.repositories;

import com.atlashub.commerce.catalog.domain.entities.Vendor;
import com.atlashub.commerce.catalog.domain.valueobject.VendorStatus;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface VendorRepository extends Repository<Vendor> {

    List<Vendor> findByOrganizationId(Long organizationId);

    List<Vendor> findByOrganizationIdAndStatus(Long organizationId, VendorStatus status);

    Optional<Vendor> findByOrganizationIdAndUserId(Long organizationId, Long userId);
}
