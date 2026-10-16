package com.atlashub.accounting.budget.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class BudgetNotFoundException extends NotFoundException {

    public BudgetNotFoundException(Long id) {
        super("Budget not found: " + id);
    }
}
