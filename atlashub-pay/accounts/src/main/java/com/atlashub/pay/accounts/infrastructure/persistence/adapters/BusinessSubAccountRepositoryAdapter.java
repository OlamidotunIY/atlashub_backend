package com.atlashub.pay.accounts.infrastructure.persistence.adapters;

import com.atlashub.pay.accounts.domain.entities.BusinessSubAccount;
import com.atlashub.pay.accounts.domain.repositories.BusinessSubAccountRepository;
import com.atlashub.pay.accounts.infrastructure.persistence.entities.BusinessSubAccountJpa;
import com.atlashub.pay.accounts.infrastructure.persistence.mappers.BusinessSubAccountMapper;
import com.atlashub.pay.accounts.infrastructure.persistence.repositories.SpringDataBusinessSubAccountRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BusinessSubAccountRepositoryAdapter
        extends JpaBaseRepository<BusinessSubAccount, BusinessSubAccountJpa>
        implements BusinessSubAccountRepository {
    private final SpringDataBusinessSubAccountRepository repository;

    public BusinessSubAccountRepositoryAdapter(SpringDataBusinessSubAccountRepository repository,
            BusinessSubAccountMapper mapper, DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher) {
        super(repository, mapper, sequenceGenerator, eventPublisher);
        this.repository = repository;
    }

    @Override protected String getSequenceName() { return "business_sub_account_seq"; }
    @Override public Optional<BusinessSubAccount> findByOrganizationId(Long organizationId) {
        return repository.findByOrganizationId(organizationId).map(mapper::toDomain);
    }
    @Override public Optional<BusinessSubAccount> findByAnchorSubAccountId(String anchorSubAccountId) {
        return repository.findByAnchorSubAccountId(anchorSubAccountId).map(mapper::toDomain);
    }
}
