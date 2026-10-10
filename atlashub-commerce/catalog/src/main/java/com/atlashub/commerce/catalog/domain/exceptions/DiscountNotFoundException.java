package com.atlashub.commerce.catalog.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class DiscountNotFoundException extends NotFoundException {
    public DiscountNotFoundException(Long id) {
        super("Discount not found: " + id);
    }
}
