package com.atlashub.pay.mandates.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class MandatePausedException extends BusinessRuleException {

    public MandatePausedException() {
        super("Payment mandate is currently paused");
    }

    public MandatePausedException(String message) {
        super(message);
    }
}
