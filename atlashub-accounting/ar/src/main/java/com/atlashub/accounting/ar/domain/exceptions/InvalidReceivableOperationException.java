package com.atlashub.accounting.ar.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidReceivableOperationException extends BusinessRuleException {

    public InvalidReceivableOperationException(String message) {
        super(message);
    }
}
