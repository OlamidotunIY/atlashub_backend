package com.atlashub.accounting.budget.application.port;

import com.atlashub.shared.domain.valueobject.Money;

import java.util.Optional;

public interface BudgetExpenditurePort {

    Optional<Money> findActualExpenditure(Long organizationId, Long accountId, String period);
}
