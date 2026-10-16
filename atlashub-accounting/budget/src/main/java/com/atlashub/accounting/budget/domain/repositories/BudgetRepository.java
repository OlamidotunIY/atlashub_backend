package com.atlashub.accounting.budget.domain.repositories;

import com.atlashub.accounting.budget.domain.entities.Budget;
import com.atlashub.accounting.budget.domain.valueobject.BudgetStatus;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends Repository<Budget> {

    List<Budget> findByOrganizationId(Long organizationId);

    List<Budget> findByOrganizationIdAndStatus(Long organizationId, BudgetStatus status);

    List<Budget> findByOrganizationIdAndPeriod(Long organizationId, String period);

    List<Budget> findByOrganizationIdAndStatusAndPeriod(Long organizationId, BudgetStatus status, String period);

    Optional<Budget> findByOrganizationIdAndAccountIdAndPeriod(Long organizationId, Long accountId, String period);
}
