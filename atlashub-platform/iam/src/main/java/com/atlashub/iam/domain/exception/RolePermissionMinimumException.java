package com.atlashub.iam.domain.exception;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class RolePermissionMinimumException extends BusinessRuleException {
    
    public RolePermissionMinimumException() {
        super("A domain error occurred");
    }

    public RolePermissionMinimumException(String message) {
        super(message);
    }

    public RolePermissionMinimumException(String message, Throwable cause) {
        super(message, cause);
    }
}
