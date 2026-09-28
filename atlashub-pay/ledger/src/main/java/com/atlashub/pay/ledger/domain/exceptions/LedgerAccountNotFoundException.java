package com.atlashub.pay.ledger.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class LedgerAccountNotFoundException extends NotFoundException {

    public LedgerAccountNotFoundException() {
        super("Ledger account not found");
    }

    public LedgerAccountNotFoundException(String message) {
        super(message);
    }

    public LedgerAccountNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
