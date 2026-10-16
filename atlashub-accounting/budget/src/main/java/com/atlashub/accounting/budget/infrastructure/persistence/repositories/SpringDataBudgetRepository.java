package com.atlashub.accounting.budget.infrastructure.persistence.repositories;

import com.atlashub.accounting.budget.domain.valueobject.BudgetStatus;
import com.atlashub.accounting.budget.infrastructure.persistence.entities.BudgetJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataBudgetRepository extends JpaRepository<BudgetJpaEntity, Long> {

    List<BudgetJpaEntity> findByOrganizationId(Long organizationId);

    List<BudgetJpaEntity> findByOrganizationIdAndStatus(Long organizationId, BudgetStatus status);

    List<BudgetJpaEntity> findByOrganizationIdAndPeriod(Long organizationId, String period);

    List<BudgetJpaEntity> findByOrganizationIdAndStatusAndPeriod(Long organizationId, BudgetStatus status, String period);

    Optional<BudgetJpaEntity> findByOrganizationIdAndAccountIdAndPeriod(Long organizationId, Long accountId, String period);
}
