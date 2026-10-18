package com.atlashub.pay.controls.domain.exceptions;

import com.atlashub.shared.domain.exception.ValidationException;

public class InvalidDecisionException extends ValidationException {

    public InvalidDecisionException() {
        super("Invalid payment control decision parameters");
    }

    public InvalidDecisionException(String message) {
        super(message);
    }
}
