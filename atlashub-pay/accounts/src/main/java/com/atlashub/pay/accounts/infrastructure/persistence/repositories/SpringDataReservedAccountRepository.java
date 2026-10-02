package com.atlashub.pay.accounts.infrastructure.persistence.repositories;

import com.atlashub.pay.accounts.domain.valueobject.ExternalAccountStatus;
import com.atlashub.pay.accounts.domain.valueobject.ReservedAccountOwnerType;
import com.atlashub.pay.accounts.infrastructure.persistence.entities.ReservedAccountJpa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SpringDataReservedAccountRepository extends JpaRepository<ReservedAccountJpa, Long>,
        JpaSpecificationExecutor<ReservedAccountJpa> {
    Optional<ReservedAccountJpa> findByOrganizationIdAndId(Long organizationId, Long id);
    Optional<ReservedAccountJpa> findByRequestReference(String requestReference);
    Optional<ReservedAccountJpa> findByAnchorReservedAccountId(String anchorReservedAccountId);
    Optional<ReservedAccountJpa> findFirstByOrganizationIdAndOwnerTypeAndOwnerReferenceIdAndProviderAndStatusNot(
            Long organizationId, ReservedAccountOwnerType ownerType, String ownerReferenceId,
            String provider, ExternalAccountStatus status);
    Page<ReservedAccountJpa> findAllByOrganizationId(Long organizationId, Pageable pageable);
    Page<ReservedAccountJpa> findAllByOrganizationIdAndOwnerTypeAndOwnerReferenceIdAndStatus(
            Long organizationId, ReservedAccountOwnerType ownerType, String ownerReferenceId,
            ExternalAccountStatus status, Pageable pageable);
}
