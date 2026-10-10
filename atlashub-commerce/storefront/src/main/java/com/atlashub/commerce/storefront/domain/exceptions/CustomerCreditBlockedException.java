package com.atlashub.commerce.storefront.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class CustomerCreditBlockedException extends BusinessRuleException {

    public CustomerCreditBlockedException() {
        super("Customer credit account is blocked");
    }

    public CustomerCreditBlockedException(Long customerId) {
        super("Customer credit account is blocked for customer: " + customerId);
    }
}
