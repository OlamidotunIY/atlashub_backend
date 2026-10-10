package com.atlashub.commerce.catalog.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class ProductNotFoundException extends NotFoundException {
    public ProductNotFoundException(Long id) {
        super("Product not found: " + id);
    }
}
