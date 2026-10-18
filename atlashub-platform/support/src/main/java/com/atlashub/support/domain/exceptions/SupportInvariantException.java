package com.atlashub.support.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class SupportInvariantException extends BusinessRuleException {
    public SupportInvariantException(String message) {
        super(message);
    }
}
