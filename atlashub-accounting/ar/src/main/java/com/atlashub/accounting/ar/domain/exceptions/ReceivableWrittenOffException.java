package com.atlashub.accounting.ar.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class ReceivableWrittenOffException extends BusinessRuleException {

    public ReceivableWrittenOffException() {
        super("Cannot process a written-off receivable");
    }

    public ReceivableWrittenOffException(String message) {
        super(message);
    }
}
