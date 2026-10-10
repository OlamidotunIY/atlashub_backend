package com.atlashub.commerce.storefront.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class CustomerDepositNotFoundException extends NotFoundException {

    public CustomerDepositNotFoundException(Long id) {
        super("Customer deposit not found: " + id);
    }
}
