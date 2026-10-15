package com.atlashub.accounting.budget.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidBudgetStateException extends BusinessRuleException {

    public InvalidBudgetStateException(String message) {
        super(message);
    }
}
