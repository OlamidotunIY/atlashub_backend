package com.atlashub.iam.domain.exception;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvitationExpiredException extends BusinessRuleException {
    
    public InvitationExpiredException() {
        super("A domain error occurred");
    }

    public InvitationExpiredException(String message) {
        super(message);
    }

    public InvitationExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
