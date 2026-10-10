package com.atlashub.commerce.storefront.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class CreditLimitExceededException extends BusinessRuleException {

    public CreditLimitExceededException() {
        super("This sale would exceed the customer's credit limit");
    }

    public CreditLimitExceededException(String message) {
        super(message);
    }
}
