package com.atlashub.pay.accounts.infrastructure.persistence.repositories;

import com.atlashub.pay.accounts.domain.valueobject.ExternalAccountStatus;
import com.atlashub.pay.accounts.domain.valueobject.ReservedAccountOwnerType;
import com.atlashub.pay.accounts.infrastructure.persistence.entities.ReservedAccountJpa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import com.atlashub.shared.application.security.ApiEnvironment;

public interface SpringDataReservedAccountRepository extends JpaRepository<ReservedAccountJpa, Long>,
        JpaSpecificationExecutor<ReservedAccountJpa> {
    Optional<ReservedAccountJpa> findByOrganizationIdAndEnvironmentAndId(Long organizationId, ApiEnvironment environment, Long id);
    Optional<ReservedAccountJpa> findByRequestReferenceAndEnvironment(String requestReference, ApiEnvironment environment);
    Optional<ReservedAccountJpa> findByAnchorReservedAccountIdAndEnvironment(String anchorReservedAccountId, ApiEnvironment environment);
    Optional<ReservedAccountJpa> findFirstByOrganizationIdAndEnvironmentAndOwnerTypeAndOwnerReferenceIdAndProviderAndStatusNot(
            Long organizationId, ApiEnvironment environment, ReservedAccountOwnerType ownerType, String ownerReferenceId,
            String provider, ExternalAccountStatus status);
    Page<ReservedAccountJpa> findAllByOrganizationIdAndEnvironment(Long organizationId, ApiEnvironment environment, Pageable pageable);
    List<ReservedAccountJpa> findByOrganizationIdAndEnvironment(Long organizationId, ApiEnvironment environment);
    List<ReservedAccountJpa> findByStatusInOrderByUpdatedAt(
            Collection<ExternalAccountStatus> statuses, Pageable pageable);
    Page<ReservedAccountJpa> findAllByOrganizationIdAndEnvironmentAndOwnerTypeAndOwnerReferenceIdAndStatus(
            Long organizationId, ApiEnvironment environment, ReservedAccountOwnerType ownerType, String ownerReferenceId,
            ExternalAccountStatus status, Pageable pageable);
}
