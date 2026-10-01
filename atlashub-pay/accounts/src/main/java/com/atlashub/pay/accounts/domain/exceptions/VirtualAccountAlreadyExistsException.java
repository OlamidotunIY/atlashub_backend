package com.atlashub.pay.accounts.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class VirtualAccountAlreadyExistsException extends BusinessRuleException {
    public VirtualAccountAlreadyExistsException(String message) {
        super(message);
    }
}
