package com.atlashub.commerce.catalog.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;
import com.atlashub.shared.domain.valueobject.Money;

public class DiscountMinimumNotMetException extends BusinessRuleException {
    public DiscountMinimumNotMetException(Money minimum) {
        super("Order does not meet the minimum amount of " + (minimum != null ? minimum.amount() : "0"));
    }

    public DiscountMinimumNotMetException(String message) {
        super(message);
    }
}
