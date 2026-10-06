package com.atlashub.pay.accounts.infrastructure.persistence.repositories;

import com.atlashub.pay.accounts.domain.valueobject.ExternalAccountStatus;
import com.atlashub.pay.accounts.infrastructure.persistence.entities.BusinessDepositAccountJpa;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import com.atlashub.shared.application.security.ApiEnvironment;

public interface SpringDataBusinessDepositAccountRepository extends JpaRepository<BusinessDepositAccountJpa, Long> {
    Optional<BusinessDepositAccountJpa> findByOrganizationIdAndEnvironment(Long organizationId, ApiEnvironment environment);
    Optional<BusinessDepositAccountJpa> findByAnchorAccountIdAndEnvironment(String anchorAccountId, ApiEnvironment environment);
    List<BusinessDepositAccountJpa> findByStatusInOrderByUpdatedAt(
            Collection<ExternalAccountStatus> statuses, Pageable pageable);
}
