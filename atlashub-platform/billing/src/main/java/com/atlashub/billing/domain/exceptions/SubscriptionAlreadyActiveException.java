package com.atlashub.billing.domain.exceptions;

import com.atlashub.shared.domain.exception.ConflictException;

public class SubscriptionAlreadyActiveException extends ConflictException {
    public SubscriptionAlreadyActiveException(String message) {
        super(message);
    }
}
