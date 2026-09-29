package com.atlashub.iam.domain.exception;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class DeleteRoleWithActiveMembersException extends BusinessRuleException {
    
    public DeleteRoleWithActiveMembersException() {
        super("A domain error occurred");
    }

    public DeleteRoleWithActiveMembersException(String message) {
        super(message);
    }

    public DeleteRoleWithActiveMembersException(String message, Throwable cause) {
        super(message, cause);
    }
}
