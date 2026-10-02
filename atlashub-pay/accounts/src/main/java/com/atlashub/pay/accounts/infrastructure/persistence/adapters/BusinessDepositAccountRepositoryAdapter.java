package com.atlashub.pay.accounts.infrastructure.persistence.adapters;

import com.atlashub.pay.accounts.domain.entities.BusinessDepositAccount;
import com.atlashub.pay.accounts.domain.repositories.BusinessDepositAccountRepository;
import com.atlashub.pay.accounts.infrastructure.persistence.entities.BusinessDepositAccountJpa;
import com.atlashub.pay.accounts.infrastructure.persistence.mappers.BusinessDepositAccountMapper;
import com.atlashub.pay.accounts.infrastructure.persistence.repositories.SpringDataBusinessDepositAccountRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BusinessDepositAccountRepositoryAdapter
        extends JpaBaseRepository<BusinessDepositAccount, BusinessDepositAccountJpa>
        implements BusinessDepositAccountRepository {
    private final SpringDataBusinessDepositAccountRepository repository;

    public BusinessDepositAccountRepositoryAdapter(SpringDataBusinessDepositAccountRepository repository,
            BusinessDepositAccountMapper mapper, DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher) {
        super(repository, mapper, sequenceGenerator, eventPublisher);
        this.repository = repository;
    }

    @Override protected String getSequenceName() { return "business_deposit_account_seq"; }
    @Override public Optional<BusinessDepositAccount> findByOrganizationId(Long organizationId) {
        return repository.findByOrganizationId(organizationId).map(mapper::toDomain);
    }
    @Override public Optional<BusinessDepositAccount> findByAnchorAccountId(String anchorAccountId) {
        return repository.findByAnchorAccountId(anchorAccountId).map(mapper::toDomain);
    }
}
