package com.atlashub.billing.domain.exceptions;

import com.atlashub.shared.domain.exception.ConflictException;

public class InvoiceAlreadyPaidException extends ConflictException {
    public InvoiceAlreadyPaidException(String message) {
        super(message);
    }
}
