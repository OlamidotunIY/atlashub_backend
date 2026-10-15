package com.atlashub.accounting.ap.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvoiceDisputedException extends BusinessRuleException {

    public InvoiceDisputedException() {
        super("This invoice is under dispute and cannot be processed");
    }

    public InvoiceDisputedException(String message) {
        super(message);
    }
}
