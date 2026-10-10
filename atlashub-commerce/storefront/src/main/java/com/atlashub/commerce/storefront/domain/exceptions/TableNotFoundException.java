package com.atlashub.commerce.storefront.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class TableNotFoundException extends NotFoundException {

    public TableNotFoundException(Long id) {
        super("Table not found: " + id);
    }
}
