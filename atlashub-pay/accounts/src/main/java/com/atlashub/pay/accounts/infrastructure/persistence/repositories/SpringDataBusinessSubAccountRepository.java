package com.atlashub.pay.accounts.infrastructure.persistence.repositories;

import com.atlashub.pay.accounts.domain.valueobject.ExternalAccountStatus;
import com.atlashub.pay.accounts.infrastructure.persistence.entities.BusinessSubAccountJpa;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import com.atlashub.shared.application.security.ApiEnvironment;

public interface SpringDataBusinessSubAccountRepository extends JpaRepository<BusinessSubAccountJpa, Long> {
    Optional<BusinessSubAccountJpa> findByOrganizationIdAndEnvironment(Long organizationId, ApiEnvironment environment);
    Optional<BusinessSubAccountJpa> findByAnchorSubAccountIdAndEnvironment(String anchorSubAccountId, ApiEnvironment environment);
    List<BusinessSubAccountJpa> findByStatusInOrderByUpdatedAt(
            Collection<ExternalAccountStatus> statuses, Pageable pageable);
}
