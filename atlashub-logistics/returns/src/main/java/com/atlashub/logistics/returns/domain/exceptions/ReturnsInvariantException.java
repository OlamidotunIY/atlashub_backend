package com.atlashub.logistics.returns.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class ReturnsInvariantException extends BusinessRuleException {

    public ReturnsInvariantException(String message) {
        super(message);
    }
}
