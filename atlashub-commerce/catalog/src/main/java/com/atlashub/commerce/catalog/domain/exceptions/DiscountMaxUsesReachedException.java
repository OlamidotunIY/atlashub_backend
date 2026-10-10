package com.atlashub.commerce.catalog.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class DiscountMaxUsesReachedException extends BusinessRuleException {
    public DiscountMaxUsesReachedException() {
        super("This discount has reached its maximum usage limit");
    }

    public DiscountMaxUsesReachedException(String message) {
        super(message);
    }
}
