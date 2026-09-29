package com.atlashub.iam.domain.exception;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidRolePermissionCountException extends BusinessRuleException {
    
    public InvalidRolePermissionCountException() {
        super("A domain error occurred");
    }

    public InvalidRolePermissionCountException(String message) {
        super(message);
    }

    public InvalidRolePermissionCountException(String message, Throwable cause) {
        super(message, cause);
    }
}
