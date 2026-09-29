package com.atlashub.pay.ledger.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InsufficientFundsException extends BusinessRuleException {

    public InsufficientFundsException() {
        super("Insufficient funds in ledger account");
    }

    public InsufficientFundsException(String message) {
        super(message);
    }

    public InsufficientFundsException(String message, Throwable cause) {
        super(message, cause);
    }
}
