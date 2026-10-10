package com.atlashub.commerce.catalog.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class PurchaseOrderNotFoundException extends NotFoundException {
    public PurchaseOrderNotFoundException(Long id) {
        super("Purchase order not found: " + id);
    }
}
