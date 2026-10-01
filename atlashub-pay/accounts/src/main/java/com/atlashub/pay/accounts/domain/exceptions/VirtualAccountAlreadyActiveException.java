package com.atlashub.pay.accounts.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class VirtualAccountAlreadyActiveException extends BusinessRuleException {
    public VirtualAccountAlreadyActiveException() {
        super("Virtual account is already active");
    }
}
