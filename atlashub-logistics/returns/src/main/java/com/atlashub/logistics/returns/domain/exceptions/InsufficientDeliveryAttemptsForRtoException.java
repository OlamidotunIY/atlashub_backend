package com.atlashub.logistics.returns.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InsufficientDeliveryAttemptsForRtoException extends BusinessRuleException {

    public InsufficientDeliveryAttemptsForRtoException(int attempts) {
        super(String.format("Failed delivery customer-fault RTO requires at least 2 confirmed delivery attempts; found: %d", attempts));
    }
}
