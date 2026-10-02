package com.atlashub.pay.ledger.domain.repositories;

import com.atlashub.pay.ledger.domain.entities.LedgerAccount;
import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountType;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface LedgerAccountRepository extends Repository<LedgerAccount> {
    Optional<LedgerAccount> findByIdWithLock(Long id);
    List<LedgerAccount> findAllByOrganizationId(Long organizationId);
    Optional<LedgerAccount> findByOrganizationIdAndAccountType(Long organizationId, LedgerAccountType type);
    Optional<LedgerAccount> findByOrganizationIdAndOutletId(Long organizationId, Long outletId);
    Optional<LedgerAccount> findByOrganizationIdAndParty(
            Long organizationId, String partyType, String partyReferenceId, LedgerAccountType type);
    List<LedgerAccount> findAllByIdInWithLock(List<Long> ids);
}
