package com.atlashub.compliance.domain.exception;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class StepOutOfOrderException extends BusinessRuleException {
    
    public StepOutOfOrderException() {
        super("A domain error occurred");
    }

    public StepOutOfOrderException(String message) {
        super(message);
    }

    public StepOutOfOrderException(String message, Throwable cause) {
        super(message, cause);
    }
}
