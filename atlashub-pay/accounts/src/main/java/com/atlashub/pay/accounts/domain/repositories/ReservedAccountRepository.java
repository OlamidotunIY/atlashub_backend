package com.atlashub.pay.accounts.domain.repositories;

import com.atlashub.pay.accounts.domain.entities.ReservedAccount;
import com.atlashub.pay.accounts.domain.valueobject.ExternalAccountStatus;
import com.atlashub.pay.accounts.domain.valueobject.ReservedAccountOwnerType;
import com.atlashub.shared.domain.repository.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import com.atlashub.shared.application.security.ApiEnvironment;

public interface ReservedAccountRepository extends Repository<ReservedAccount> {
    Optional<ReservedAccount> findByOrganizationIdAndEnvironmentAndId(Long organizationId, ApiEnvironment environment, Long id);
    Optional<ReservedAccount> findByRequestReferenceAndEnvironment(String requestReference, ApiEnvironment environment);
    Optional<ReservedAccount> findByAnchorReservedAccountIdAndEnvironment(String anchorReservedAccountId, ApiEnvironment environment);
    Optional<ReservedAccount> findActiveByOwner(
            Long organizationId, ApiEnvironment environment, ReservedAccountOwnerType ownerType, String ownerReferenceId, String provider);
    Page<ReservedAccount> search(Long organizationId, ApiEnvironment environment, ReservedAccountOwnerType ownerType,
                                 String ownerReferenceId, ExternalAccountStatus status, Pageable pageable);
    List<ReservedAccount> findByOrganizationIdAndEnvironment(Long organizationId, ApiEnvironment environment);
    List<ReservedAccount> findPendingReconciliation(int limit);
}
