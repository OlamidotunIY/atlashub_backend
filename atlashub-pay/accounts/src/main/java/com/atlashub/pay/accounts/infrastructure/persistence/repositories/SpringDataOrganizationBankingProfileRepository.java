package com.atlashub.pay.accounts.infrastructure.persistence.repositories;

import com.atlashub.pay.accounts.infrastructure.persistence.entities.OrganizationBankingProfileJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataOrganizationBankingProfileRepository
        extends JpaRepository<OrganizationBankingProfileJpa, Long> {
    Optional<OrganizationBankingProfileJpa> findByOrganizationId(Long organizationId);
}
