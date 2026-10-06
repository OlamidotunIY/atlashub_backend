package com.atlashub.accounts.infrastructure.persistence.repositories;

import com.atlashub.accounts.infrastructure.persistence.entities.OutletJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataOutletRepository extends JpaRepository<OutletJpa, Long> {
    List<OutletJpa> findAllByOrganizationId(Long organizationId);
}
