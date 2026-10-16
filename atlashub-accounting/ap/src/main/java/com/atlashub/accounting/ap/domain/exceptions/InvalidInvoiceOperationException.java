package com.atlashub.accounting.ap.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidInvoiceOperationException extends BusinessRuleException {

    public InvalidInvoiceOperationException(String message) {
        super(message);
    }
}
