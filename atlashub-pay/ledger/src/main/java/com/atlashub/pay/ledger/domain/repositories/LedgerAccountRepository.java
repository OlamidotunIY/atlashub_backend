package com.atlashub.pay.ledger.domain.repositories;

import com.atlashub.pay.ledger.domain.entities.LedgerAccount;
import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountType;
import com.atlashub.pay.ledger.domain.valueobject.LedgerPartyType;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface LedgerAccountRepository extends Repository<LedgerAccount> {
    Optional<LedgerAccount> findByIdWithLock(Long id);
    List<LedgerAccount> findAllByOrganizationId(Long organizationId);
    List<LedgerAccount> findAllByOrganizationIdAndEnvironment(Long organizationId, ApiEnvironment environment);
    Optional<LedgerAccount> findByOrganizationIdAndEnvironmentAndAccountTypeAndCurrency(
            Long organizationId, ApiEnvironment environment, LedgerAccountType type, CurrencyCode currency);
    Optional<LedgerAccount> findByOrganizationIdAndEnvironmentAndOutletIdAndCurrency(
            Long organizationId, ApiEnvironment environment, Long outletId, CurrencyCode currency);
    Optional<LedgerAccount> findByOrganizationIdAndEnvironmentAndParty(
            Long organizationId, ApiEnvironment environment, LedgerPartyType partyType,
            String partyReferenceId, LedgerAccountType type, CurrencyCode currency);
    List<LedgerAccount> findAllByIdInWithLock(List<Long> ids);
}
