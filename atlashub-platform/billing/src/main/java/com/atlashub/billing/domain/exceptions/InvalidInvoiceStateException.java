package com.atlashub.billing.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidInvoiceStateException extends BusinessRuleException {
    public InvalidInvoiceStateException(String message) {
        super(message);
    }
}
