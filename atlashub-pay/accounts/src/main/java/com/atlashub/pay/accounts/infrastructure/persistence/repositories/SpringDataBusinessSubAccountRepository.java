package com.atlashub.pay.accounts.infrastructure.persistence.repositories;

import com.atlashub.pay.accounts.infrastructure.persistence.entities.BusinessSubAccountJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataBusinessSubAccountRepository extends JpaRepository<BusinessSubAccountJpa, Long> {
    Optional<BusinessSubAccountJpa> findByOrganizationId(Long organizationId);
    Optional<BusinessSubAccountJpa> findByAnchorSubAccountId(String anchorSubAccountId);
}
