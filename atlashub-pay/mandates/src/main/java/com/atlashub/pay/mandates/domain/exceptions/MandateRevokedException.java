package com.atlashub.pay.mandates.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class MandateRevokedException extends BusinessRuleException {

    public MandateRevokedException() {
        super("Payment mandate has been revoked");
    }

    public MandateRevokedException(String message) {
        super(message);
    }
}
