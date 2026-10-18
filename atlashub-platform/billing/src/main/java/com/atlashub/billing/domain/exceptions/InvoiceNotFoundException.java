package com.atlashub.billing.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class InvoiceNotFoundException extends NotFoundException {
    public InvoiceNotFoundException(String message) {
        super(message);
    }
}
