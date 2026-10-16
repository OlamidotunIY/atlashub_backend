package com.atlashub.accounting.ar.domain.exceptions;

import com.atlashub.shared.domain.exception.ConflictException;

public class ReceivableAlreadyCollectedException extends ConflictException {

    public ReceivableAlreadyCollectedException() {
        super("This receivable has already been collected");
    }

    public ReceivableAlreadyCollectedException(String message) {
        super(message);
    }
}
