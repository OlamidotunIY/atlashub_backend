package com.atlashub.authentication.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class CredentialReferenceUnavailableException extends BusinessRuleException {
    public CredentialReferenceUnavailableException() {
        super("Registration credential is unavailable or already claimed");
    }
}
