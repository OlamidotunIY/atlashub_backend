package com.atlashub.compliance.domain.exception;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class StepNotCompleteException extends BusinessRuleException {
    
    public StepNotCompleteException() {
        super("A domain error occurred");
    }

    public StepNotCompleteException(String message) {
        super(message);
    }

    public StepNotCompleteException(String message, Throwable cause) {
        super(message, cause);
    }
}
