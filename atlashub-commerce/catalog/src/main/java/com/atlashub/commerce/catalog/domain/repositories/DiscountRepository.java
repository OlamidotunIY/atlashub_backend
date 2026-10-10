package com.atlashub.commerce.catalog.domain.repositories;

import com.atlashub.commerce.catalog.domain.entities.Discount;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface DiscountRepository extends Repository<Discount> {

    Optional<Discount> findByOrganizationIdAndName(Long organizationId, String name);

    List<Discount> findActiveByOrganizationId(Long organizationId);
}
