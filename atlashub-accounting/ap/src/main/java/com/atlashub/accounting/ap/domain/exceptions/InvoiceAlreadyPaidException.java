package com.atlashub.accounting.ap.domain.exceptions;

import com.atlashub.shared.domain.exception.ConflictException;

public class InvoiceAlreadyPaidException extends ConflictException {

    public InvoiceAlreadyPaidException() {
        super("This supplier invoice has already been paid");
    }

    public InvoiceAlreadyPaidException(String message) {
        super(message);
    }
}
