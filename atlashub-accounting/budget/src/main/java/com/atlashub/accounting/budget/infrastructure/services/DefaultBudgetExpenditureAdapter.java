package com.atlashub.accounting.budget.infrastructure.services;

import com.atlashub.accounting.budget.application.port.BudgetExpenditurePort;
import com.atlashub.shared.application.port.GlAccountQueryPort;
import com.atlashub.shared.domain.valueobject.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class DefaultBudgetExpenditureAdapter implements BudgetExpenditurePort {

    private final GlAccountQueryPort glAccountQueryPort;

    public DefaultBudgetExpenditureAdapter(@Autowired(required = false) GlAccountQueryPort glAccountQueryPort) {
        this.glAccountQueryPort = glAccountQueryPort;
    }

    @Override
    public Optional<Money> findActualExpenditure(Long organizationId, Long accountId, String period) {
        if (glAccountQueryPort == null || organizationId == null || accountId == null) {
            return Optional.empty();
        }

        return glAccountQueryPort.getAccountBalance(organizationId, String.valueOf(accountId), LocalDate.now());
    }
}
