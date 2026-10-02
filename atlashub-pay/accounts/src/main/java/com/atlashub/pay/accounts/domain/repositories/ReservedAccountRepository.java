package com.atlashub.pay.accounts.domain.repositories;

import com.atlashub.pay.accounts.domain.entities.ReservedAccount;
import com.atlashub.pay.accounts.domain.valueobject.ExternalAccountStatus;
import com.atlashub.pay.accounts.domain.valueobject.ReservedAccountOwnerType;
import com.atlashub.shared.domain.repository.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ReservedAccountRepository extends Repository<ReservedAccount> {
    Optional<ReservedAccount> findByOrganizationIdAndId(Long organizationId, Long id);
    Optional<ReservedAccount> findByRequestReference(String requestReference);
    Optional<ReservedAccount> findByAnchorReservedAccountId(String anchorReservedAccountId);
    Optional<ReservedAccount> findActiveByOwner(
            Long organizationId, ReservedAccountOwnerType ownerType, String ownerReferenceId, String provider);
    Page<ReservedAccount> search(Long organizationId, ReservedAccountOwnerType ownerType,
                                 String ownerReferenceId, ExternalAccountStatus status, Pageable pageable);
}
