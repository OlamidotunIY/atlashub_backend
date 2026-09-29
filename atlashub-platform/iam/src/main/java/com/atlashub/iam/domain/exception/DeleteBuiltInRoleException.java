package com.atlashub.iam.domain.exception;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class DeleteBuiltInRoleException extends BusinessRuleException {
    
    public DeleteBuiltInRoleException() {
        super("A domain error occurred");
    }

    public DeleteBuiltInRoleException(String message) {
        super(message);
    }

    public DeleteBuiltInRoleException(String message, Throwable cause) {
        super(message, cause);
    }
}
