package com.atlashub.compliance.domain.exception;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidProviderOnboardingStateException extends BusinessRuleException {
    public InvalidProviderOnboardingStateException(String message) {
        super(message);
    }
}
