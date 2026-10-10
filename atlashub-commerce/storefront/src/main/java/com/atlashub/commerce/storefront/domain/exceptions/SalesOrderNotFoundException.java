package com.atlashub.commerce.storefront.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class SalesOrderNotFoundException extends NotFoundException {

    public SalesOrderNotFoundException(Long id) {
        super("Sales order not found: " + id);
    }
}
