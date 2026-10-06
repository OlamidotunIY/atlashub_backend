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
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.pay.accounts.domain.valueobject.ExternalAccountStatus;

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
    @Override public Optional<BusinessDepositAccount> findByOrganizationIdAndEnvironment(Long organizationId, ApiEnvironment environment) {
        return repository.findByOrganizationIdAndEnvironment(organizationId, environment).map(mapper::toDomain);
    }
    @Override public Optional<BusinessDepositAccount> findByAnchorAccountIdAndEnvironment(String anchorAccountId, ApiEnvironment environment) {
        return repository.findByAnchorAccountIdAndEnvironment(anchorAccountId, environment).map(mapper::toDomain);
    }
    @Override public List<BusinessDepositAccount> findPendingReconciliation(int limit) {
        return repository.findByStatusInOrderByUpdatedAt(List.of(ExternalAccountStatus.PENDING), PageRequest.of(0, limit))
                .stream().map(mapper::toDomain).toList();
    }
}
