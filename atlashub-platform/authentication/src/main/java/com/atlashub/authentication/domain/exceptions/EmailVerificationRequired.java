package com.atlashub.authentication.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class EmailVerificationRequired extends BusinessRuleException {
    public EmailVerificationRequired() {
        super("Email verification is required to login");
    }

    public EmailVerificationRequired(String message, Throwable cause) {
        super(message, cause);
    }
}
