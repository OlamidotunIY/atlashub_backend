package com.atlashub.pay.accounts.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidProviderProfileStateException extends BusinessRuleException {
    public InvalidProviderProfileStateException(String message) {
        super(message);
    }
}
