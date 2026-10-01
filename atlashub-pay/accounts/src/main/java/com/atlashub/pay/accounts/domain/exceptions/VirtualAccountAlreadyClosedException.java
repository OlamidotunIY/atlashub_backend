package com.atlashub.pay.accounts.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class VirtualAccountAlreadyClosedException extends BusinessRuleException {
    public VirtualAccountAlreadyClosedException() { super("Virtual account is already closed"); }
}
