package com.atlashub.iam.domain.exception;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class LastOwnerDeactivationException extends BusinessRuleException {
    
    public LastOwnerDeactivationException() {
        super("A domain error occurred");
    }

    public LastOwnerDeactivationException(String message) {
        super(message);
    }

    public LastOwnerDeactivationException(String message, Throwable cause) {
        super(message, cause);
    }
}
