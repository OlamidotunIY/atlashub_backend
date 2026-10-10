package com.atlashub.accounting.gl.infrastructure.persistence.adapters;

import com.atlashub.accounting.gl.domain.entities.Account;
import com.atlashub.accounting.gl.domain.repositories.AccountRepository;
import com.atlashub.accounting.gl.infrastructure.persistence.entities.AccountJpa;
import com.atlashub.accounting.gl.infrastructure.persistence.mappers.AccountMapper;
import com.atlashub.accounting.gl.infrastructure.persistence.repositories.SpringDataAccountRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AccountRepositoryAdapter
        extends JpaBaseRepository<Account, AccountJpa>
        implements AccountRepository {

    private final SpringDataAccountRepository springDataRepo;

    public AccountRepositoryAdapter(SpringDataAccountRepository springDataRepo,
                                  AccountMapper mapper,
                                  DomainSequenceGenerator sequenceGenerator,
                                  DomainEventPublisher eventPublisher) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "accounting_account_seq";
    }

    @Override
    public Optional<Account> findByOrganizationIdAndCode(Long organizationId, String code) {
        return springDataRepo.findByOrganizationIdAndCode(organizationId, code).map(mapper::toDomain);
    }

    @Override
    public List<Account> findByOrganizationId(Long organizationId) {
        return springDataRepo.findByOrganizationId(organizationId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Account> findByOrganizationIdAndIsActiveTrue(Long organizationId) {
        return springDataRepo.findByOrganizationIdAndIsActiveTrue(organizationId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
