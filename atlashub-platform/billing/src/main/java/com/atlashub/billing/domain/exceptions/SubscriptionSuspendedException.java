package com.atlashub.billing.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class SubscriptionSuspendedException extends BusinessRuleException {
    public SubscriptionSuspendedException(String message) {
        super(message);
    }
}
