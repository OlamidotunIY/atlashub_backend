package com.atlashub.accounting.ap.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class SupplierInvoiceNotFoundException extends NotFoundException {

    public SupplierInvoiceNotFoundException(Long id) {
        super("Supplier invoice not found: " + id);
    }
}
