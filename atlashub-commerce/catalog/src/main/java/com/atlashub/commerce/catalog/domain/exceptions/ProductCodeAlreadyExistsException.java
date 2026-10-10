package com.atlashub.commerce.catalog.domain.exceptions;

import com.atlashub.shared.domain.exception.ConflictException;

public class ProductCodeAlreadyExistsException extends ConflictException {
    public ProductCodeAlreadyExistsException(String code) {
        super("Product code already exists: " + code);
    }
}
