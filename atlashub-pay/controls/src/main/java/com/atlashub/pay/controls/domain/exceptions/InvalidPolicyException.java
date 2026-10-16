package com.atlashub.pay.controls.domain.exceptions;

import com.atlashub.shared.domain.exception.ValidationException;

public class InvalidPolicyException extends ValidationException {

    public InvalidPolicyException() {
        super("Invalid controlled funds policy configuration");
    }

    public InvalidPolicyException(String message) {
        super(message);
    }
}
