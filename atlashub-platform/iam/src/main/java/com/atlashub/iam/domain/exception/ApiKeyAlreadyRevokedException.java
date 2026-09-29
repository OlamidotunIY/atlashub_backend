package com.atlashub.iam.domain.exception;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class ApiKeyAlreadyRevokedException extends BusinessRuleException {
    
    public ApiKeyAlreadyRevokedException() {
        super("A domain error occurred");
    }

    public ApiKeyAlreadyRevokedException(String message) {
        super(message);
    }

    public ApiKeyAlreadyRevokedException(String message, Throwable cause) {
        super(message, cause);
    }
}
