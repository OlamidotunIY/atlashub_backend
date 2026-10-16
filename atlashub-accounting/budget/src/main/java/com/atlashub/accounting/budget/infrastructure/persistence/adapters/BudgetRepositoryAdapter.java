package com.atlashub.accounting.budget.infrastructure.persistence.adapters;

import com.atlashub.accounting.budget.domain.entities.Budget;
import com.atlashub.accounting.budget.domain.repositories.BudgetRepository;
import com.atlashub.accounting.budget.domain.valueobject.BudgetStatus;
import com.atlashub.accounting.budget.infrastructure.persistence.entities.BudgetJpaEntity;
import com.atlashub.accounting.budget.infrastructure.persistence.mappers.BudgetMapper;
import com.atlashub.accounting.budget.infrastructure.persistence.repositories.SpringDataBudgetRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class BudgetRepositoryAdapter extends JpaBaseRepository<Budget, BudgetJpaEntity> implements BudgetRepository {

    private final SpringDataBudgetRepository springDataRepo;

    public BudgetRepositoryAdapter(
            SpringDataBudgetRepository springDataRepo,
            BudgetMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "accounting_budget_seq";
    }

    @Override
    public List<Budget> findByOrganizationId(Long organizationId) {
        return springDataRepo.findByOrganizationId(organizationId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Budget> findByOrganizationIdAndStatus(Long organizationId, BudgetStatus status) {
        return springDataRepo.findByOrganizationIdAndStatus(organizationId, status)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Budget> findByOrganizationIdAndPeriod(Long organizationId, String period) {
        return springDataRepo.findByOrganizationIdAndPeriod(organizationId, period)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Budget> findByOrganizationIdAndStatusAndPeriod(Long organizationId, BudgetStatus status, String period) {
        return springDataRepo.findByOrganizationIdAndStatusAndPeriod(organizationId, status, period)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Budget> findByOrganizationIdAndAccountIdAndPeriod(Long organizationId, Long accountId, String period) {
        return springDataRepo.findByOrganizationIdAndAccountIdAndPeriod(organizationId, accountId, period)
                .map(mapper::toDomain);
    }
}
