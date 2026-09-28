package com.atlashub.pay.ledger.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class CurrencyMismatchException extends BusinessRuleException {

    public CurrencyMismatchException() {
        super("All entries in a ledger transaction must use the same currency");
    }

    public CurrencyMismatchException(String message) {
        super(message);
    }

    public CurrencyMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
