package com.atlashub.pay.accounts.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidBankingStateException extends BusinessRuleException {
    public InvalidBankingStateException(String message) { super(message); }
}
