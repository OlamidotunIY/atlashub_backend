package com.atlashub.iam.domain.exception;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class LiveApiKeyUnavailableException extends BusinessRuleException {
    public LiveApiKeyUnavailableException() { super("Live API keys require approved compliance"); }
    public LiveApiKeyUnavailableException(String message) { super(message); }
}
