package com.atlashub.commerce.catalog.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class VendorNotFoundException extends NotFoundException {
    public VendorNotFoundException(Long id) {
        super("Vendor not found: " + id);
    }
}
