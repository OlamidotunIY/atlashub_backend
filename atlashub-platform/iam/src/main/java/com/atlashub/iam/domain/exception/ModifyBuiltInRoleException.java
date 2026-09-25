package com.atlashub.iam.domain.exception;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class ModifyBuiltInRoleException extends BusinessRuleException {
    
    public ModifyBuiltInRoleException() {
        super("A domain error occurred");
    }

    public ModifyBuiltInRoleException(String message) {
        super(message);
    }

    public ModifyBuiltInRoleException(String message, Throwable cause) {
        super(message, cause);
    }
}
