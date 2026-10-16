package com.atlashub.accounting.budget.domain.exceptions;

import com.atlashub.shared.domain.exception.ConflictException;

public class BudgetAlreadyApprovedException extends ConflictException {

    public BudgetAlreadyApprovedException() {
        super("This budget has already been approved");
    }
}
