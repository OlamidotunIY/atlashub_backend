package com.atlashub.commerce.storefront.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class CustomerCreditNotFoundException extends NotFoundException {

    public CustomerCreditNotFoundException(Long id) {
        super("Customer credit not found: " + id);
    }
}
