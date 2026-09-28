package com.atlashub.pay.ledger.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class LedgerAccountFrozenException extends BusinessRuleException {

    public LedgerAccountFrozenException() {
        super("Ledger account is frozen and cannot accept transactions");
    }

    public LedgerAccountFrozenException(String message) {
        super(message);
    }

    public LedgerAccountFrozenException(String message, Throwable cause) {
        super(message, cause);
    }
}
