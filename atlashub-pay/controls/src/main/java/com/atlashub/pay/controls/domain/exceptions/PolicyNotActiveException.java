package com.atlashub.pay.controls.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class PolicyNotActiveException extends BusinessRuleException {

    public PolicyNotActiveException() {
        super("Controlled funds policy is not active");
    }

    public PolicyNotActiveException(String message) {
        super(message);
    }
}
