package com.atlashub.pay.accounts.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class VirtualAccountNotActiveException extends BusinessRuleException {
    public VirtualAccountNotActiveException() {
        super("Virtual account is not active");
    }
}
