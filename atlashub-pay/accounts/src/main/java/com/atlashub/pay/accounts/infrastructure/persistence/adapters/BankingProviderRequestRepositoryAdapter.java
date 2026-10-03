package com.atlashub.pay.accounts.infrastructure.persistence.adapters;

import com.atlashub.pay.accounts.domain.entities.BankingProviderRequest;
import com.atlashub.pay.accounts.domain.entities.BankingProviderRequest.RequestStatus;
import com.atlashub.pay.accounts.domain.repositories.BankingProviderRequestRepository;
import com.atlashub.pay.accounts.infrastructure.persistence.entities.BankingProviderRequestJpa;
import com.atlashub.pay.accounts.infrastructure.persistence.mappers.BankingProviderRequestMapper;
import com.atlashub.pay.accounts.infrastructure.persistence.repositories.SpringDataBankingProviderRequestRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class BankingProviderRequestRepositoryAdapter
        extends JpaBaseRepository<BankingProviderRequest, BankingProviderRequestJpa>
        implements BankingProviderRequestRepository {
    private final SpringDataBankingProviderRequestRepository repository;

    public BankingProviderRequestRepositoryAdapter(SpringDataBankingProviderRequestRepository repository,
            BankingProviderRequestMapper mapper, DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher) {
        super(repository, mapper, sequenceGenerator, eventPublisher);
        this.repository = repository;
    }

    @Override protected String getSequenceName() { return "banking_provider_request_seq"; }
    @Override public Optional<BankingProviderRequest> findByRequestReferenceAndApiEnvironment(String requestReference, String apiEnvironment) {
        return repository.findByRequestReferenceAndApiEnvironment(requestReference, apiEnvironment).map(mapper::toDomain);
    }
    @Override public List<BankingProviderRequest> findPending(int limit) {
        return repository.findByStatusOrderByCreatedAt(RequestStatus.PENDING, PageRequest.of(0, limit))
                .stream().map(mapper::toDomain).toList();
    }
}
