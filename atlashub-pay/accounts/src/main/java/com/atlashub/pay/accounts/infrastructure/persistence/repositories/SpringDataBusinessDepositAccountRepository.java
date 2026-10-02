package com.atlashub.pay.accounts.infrastructure.persistence.repositories;

import com.atlashub.pay.accounts.infrastructure.persistence.entities.BusinessDepositAccountJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataBusinessDepositAccountRepository extends JpaRepository<BusinessDepositAccountJpa, Long> {
    Optional<BusinessDepositAccountJpa> findByOrganizationId(Long organizationId);
    Optional<BusinessDepositAccountJpa> findByAnchorAccountId(String anchorAccountId);
}
