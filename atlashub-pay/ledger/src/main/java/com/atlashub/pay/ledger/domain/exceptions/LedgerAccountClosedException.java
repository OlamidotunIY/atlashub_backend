package com.atlashub.pay.ledger.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class LedgerAccountClosedException extends BusinessRuleException {

    public LedgerAccountClosedException() {
        super("Ledger account is closed");
    }

    public LedgerAccountClosedException(String message) {
        super(message);
    }

    public LedgerAccountClosedException(String message, Throwable cause) {
        super(message, cause);
    }
}
