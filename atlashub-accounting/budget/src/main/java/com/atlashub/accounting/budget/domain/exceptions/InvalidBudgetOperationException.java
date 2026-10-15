package com.atlashub.accounting.budget.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidBudgetOperationException extends BusinessRuleException {

    public InvalidBudgetOperationException(String message) {
        super(message);
    }
}
