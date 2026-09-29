package com.atlashub.iam.domain.exception;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidInvitationStateException extends BusinessRuleException {
    
    public InvalidInvitationStateException() {
        super("A domain error occurred");
    }

    public InvalidInvitationStateException(String message) {
        super(message);
    }

    public InvalidInvitationStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
