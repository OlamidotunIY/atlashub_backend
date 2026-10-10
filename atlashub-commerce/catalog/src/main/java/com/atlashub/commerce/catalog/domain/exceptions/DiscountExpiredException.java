package com.atlashub.commerce.catalog.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class DiscountExpiredException extends BusinessRuleException {
    public DiscountExpiredException() {
        super("This discount has expired");
    }

    public DiscountExpiredException(String message) {
        super(message);
    }
}
