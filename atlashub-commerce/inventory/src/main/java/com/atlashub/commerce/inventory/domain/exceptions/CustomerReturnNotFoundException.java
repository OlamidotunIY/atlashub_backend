package com.atlashub.commerce.inventory.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class CustomerReturnNotFoundException extends NotFoundException {

    public CustomerReturnNotFoundException(Long id) {
        super("Customer return not found: " + id);
    }
}
