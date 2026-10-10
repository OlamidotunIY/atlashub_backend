package com.atlashub.commerce.catalog.infrastructure.persistence.repositories;

import com.atlashub.commerce.catalog.infrastructure.persistence.entities.DiscountJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataDiscountRepository extends JpaRepository<DiscountJpa, Long> {

    Optional<DiscountJpa> findByOrganizationIdAndName(Long organizationId, String name);

    List<DiscountJpa> findByOrganizationIdAndActiveTrue(Long organizationId);
}
