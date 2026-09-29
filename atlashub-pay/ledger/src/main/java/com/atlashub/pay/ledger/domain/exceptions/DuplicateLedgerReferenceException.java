package com.atlashub.pay.ledger.domain.exceptions;

import com.atlashub.shared.domain.exception.ConflictException;

public class DuplicateLedgerReferenceException extends ConflictException {
    
    public DuplicateLedgerReferenceException(String reference) {
        super("A ledger transaction with reference '" + reference + "' already exists");
    }

    public DuplicateLedgerReferenceException(String reference, Throwable cause) {
        super("A ledger transaction with reference '" + reference + "' already exists", cause);
    }
}
