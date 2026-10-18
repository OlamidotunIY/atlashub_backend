package com.atlashub.pay.controls.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidControlTransitionException extends BusinessRuleException {

    public InvalidControlTransitionException() {
        super("Invalid control state transition");
    }

    public InvalidControlTransitionException(String message) {
        super(message);
    }
}
