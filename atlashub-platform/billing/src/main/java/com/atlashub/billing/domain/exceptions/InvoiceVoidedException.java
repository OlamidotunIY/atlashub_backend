package com.atlashub.billing.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvoiceVoidedException extends BusinessRuleException {
    public InvoiceVoidedException(String message) {
        super(message);
    }
}
