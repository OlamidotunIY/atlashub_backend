package com.atlashub.billing.domain.exceptions;

import com.atlashub.shared.domain.exception.DomainException;

public class BillingDomainException extends DomainException {
    public BillingDomainException(String message) {
        super(message);
    }
}
