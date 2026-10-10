package com.atlashub.commerce.catalog.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class SupplierNotFoundException extends NotFoundException {
    public SupplierNotFoundException(Long id) {
        super("Supplier not found: " + id);
    }
}
