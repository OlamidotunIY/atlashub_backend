package com.atlashub.billing.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class SubscriptionNotFoundException extends NotFoundException {
    public SubscriptionNotFoundException(String message) {
        super(message);
    }
}
