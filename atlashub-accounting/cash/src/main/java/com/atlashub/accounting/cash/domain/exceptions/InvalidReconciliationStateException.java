package com.atlashub.accounting.cash.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidReconciliationStateException extends BusinessRuleException {

    public InvalidReconciliationStateException(String message) {
        super(message);
    }
}
