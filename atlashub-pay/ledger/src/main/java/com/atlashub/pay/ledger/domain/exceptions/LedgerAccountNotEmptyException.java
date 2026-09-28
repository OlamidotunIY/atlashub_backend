package com.atlashub.pay.ledger.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class LedgerAccountNotEmptyException extends BusinessRuleException {

    public LedgerAccountNotEmptyException() {
        super("Ledger account cannot be closed while it has a non-zero balance");
    }

    public LedgerAccountNotEmptyException(String message) {
        super(message);
    }

    public LedgerAccountNotEmptyException(String message, Throwable cause) {
        super(message, cause);
    }
}
