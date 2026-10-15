package com.atlashub.accounting.budget.presentation.dto;

import com.atlashub.accounting.budget.domain.valueobject.BudgetStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record BudgetResponse(
        Long id,
        Long organizationId,
        Long accountId,
        String name,
        String period,
        BigDecimal budgetedAmount,
        String currency,
        BudgetStatus status,
        Long initiatedBy,
        Long approvedBy,
        ZonedDateTime approvedAt
) {}
