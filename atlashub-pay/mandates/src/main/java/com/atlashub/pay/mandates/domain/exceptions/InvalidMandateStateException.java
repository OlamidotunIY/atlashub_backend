package com.atlashub.pay.mandates.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidMandateStateException extends BusinessRuleException {

    public InvalidMandateStateException(String message) {
        super(message);
    }
}
