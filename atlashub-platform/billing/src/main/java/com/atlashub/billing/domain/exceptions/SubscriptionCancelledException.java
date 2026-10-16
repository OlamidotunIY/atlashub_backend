package com.atlashub.billing.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class SubscriptionCancelledException extends BusinessRuleException {
    public SubscriptionCancelledException(String message) {
        super(message);
    }
}
