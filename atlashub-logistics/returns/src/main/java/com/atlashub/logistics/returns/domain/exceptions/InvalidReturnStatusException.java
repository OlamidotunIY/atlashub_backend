package com.atlashub.logistics.returns.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidReturnStatusException extends BusinessRuleException {

    public InvalidReturnStatusException(String message) {
        super(message);
    }
}
