package com.atlashub.pay.ledger.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class UnbalancedLedgerTransactionException extends BusinessRuleException {

    public UnbalancedLedgerTransactionException() {
        super("A domain error occurred");
    }

    public UnbalancedLedgerTransactionException(String message) {
        super(message);
    }

    public UnbalancedLedgerTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
