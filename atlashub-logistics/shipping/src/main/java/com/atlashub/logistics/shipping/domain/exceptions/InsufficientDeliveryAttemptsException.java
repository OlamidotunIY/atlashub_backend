package com.atlashub.logistics.shipping.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InsufficientDeliveryAttemptsException extends BusinessRuleException {

    public InsufficientDeliveryAttemptsException(int currentAttempts) {
        super("At least 2 failed delivery attempts are required before marking customer-fault delivery failure. Current attempts: " + currentAttempts);
    }
}
