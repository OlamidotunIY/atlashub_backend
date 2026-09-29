package com.atlashub.iam.domain.exception;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class RoleModificationException extends BusinessRuleException {
    
    public RoleModificationException() {
        super("A domain error occurred");
    }

    public RoleModificationException(String message) {
        super(message);
    }

    public RoleModificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
