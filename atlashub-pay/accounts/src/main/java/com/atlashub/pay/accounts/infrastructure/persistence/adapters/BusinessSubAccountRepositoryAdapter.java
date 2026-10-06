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
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.pay.accounts.domain.valueobject.ExternalAccountStatus;

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
    @Override public Optional<BusinessSubAccount> findByOrganizationIdAndEnvironment(Long organizationId, ApiEnvironment environment) {
        return repository.findByOrganizationIdAndEnvironment(organizationId, environment).map(mapper::toDomain);
    }
    @Override public Optional<BusinessSubAccount> findByAnchorSubAccountIdAndEnvironment(String anchorSubAccountId, ApiEnvironment environment) {
        return repository.findByAnchorSubAccountIdAndEnvironment(anchorSubAccountId, environment).map(mapper::toDomain);
    }
    @Override public List<BusinessSubAccount> findPendingReconciliation(int limit) {
        return repository.findByStatusInOrderByUpdatedAt(List.of(ExternalAccountStatus.PENDING), PageRequest.of(0, limit))
                .stream().map(mapper::toDomain).toList();
    }
}
