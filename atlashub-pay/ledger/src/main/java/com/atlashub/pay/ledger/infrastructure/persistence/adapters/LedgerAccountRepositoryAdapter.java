package com.atlashub.pay.ledger.infrastructure.persistence.adapters;

import com.atlashub.pay.ledger.domain.entities.LedgerAccount;
import com.atlashub.pay.ledger.domain.repositories.LedgerAccountRepository;
import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountType;
import com.atlashub.pay.ledger.domain.valueobject.LedgerPartyType;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.pay.ledger.infrastructure.persistence.entities.LedgerAccountJpa;
import com.atlashub.pay.ledger.infrastructure.persistence.mappers.LedgerAccountMapper;
import com.atlashub.pay.ledger.infrastructure.persistence.repositories.SpringDataLedgerAccountRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class LedgerAccountRepositoryAdapter
        extends JpaBaseRepository<LedgerAccount, LedgerAccountJpa>
        implements LedgerAccountRepository {

    private final SpringDataLedgerAccountRepository springDataRepo;

    public LedgerAccountRepositoryAdapter(SpringDataLedgerAccountRepository springDataRepo,
                                          LedgerAccountMapper mapper,
                                          DomainSequenceGenerator sequenceGenerator,
                                          DomainEventPublisher eventPublisher) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "ledger_account_seq";
    }

    @Override
    public Optional<LedgerAccount> findByIdWithLock(Long id) {
        return springDataRepo.findByIdForUpdate(id).map(mapper::toDomain);
    }

    @Override
    public List<LedgerAccount> findAllByOrganizationId(Long organizationId) {
        return springDataRepo.findAllByOrganizationId(organizationId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<LedgerAccount> findAllByOrganizationIdAndEnvironment(Long organizationId, ApiEnvironment environment) {
        return springDataRepo.findAllByOrganizationIdAndEnvironment(organizationId, environment).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public Optional<LedgerAccount> findByOrganizationIdAndEnvironmentAndAccountTypeAndCurrency(
            Long organizationId, ApiEnvironment environment, LedgerAccountType type, CurrencyCode currency) {
        return springDataRepo.findByOrganizationIdAndEnvironmentAndAccountTypeAndCurrency(
                        organizationId, environment, type.name(), currency.name())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<LedgerAccount> findByOrganizationIdAndEnvironmentAndOutletIdAndCurrency(
            Long organizationId, ApiEnvironment environment, Long outletId, CurrencyCode currency) {
        return springDataRepo.findByOrganizationIdAndEnvironmentAndOutletIdAndCurrency(
                        organizationId, environment, outletId, currency.name())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<LedgerAccount> findByOrganizationIdAndEnvironmentAndParty(
            Long organizationId, ApiEnvironment environment, LedgerPartyType partyType,
            String partyReferenceId, LedgerAccountType type, CurrencyCode currency) {
        return springDataRepo.findByOrganizationIdAndEnvironmentAndPartyTypeAndPartyReferenceIdAndAccountTypeAndCurrency(
                        organizationId, environment, partyType.name(), partyReferenceId, type.name(), currency.name())
                .map(mapper::toDomain);
    }

    @Override
    public List<LedgerAccount> findAllByIdInWithLock(List<Long> ids) {
        return springDataRepo.findAllByIdIn(ids).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
