package com.atlashub.pay.ledger.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class LedgerInvariantException extends BusinessRuleException {
    public LedgerInvariantException(String message) {
        super(message);
    }

    public LedgerInvariantException(String message, Throwable cause) {
        super(message, cause);
    }
}
