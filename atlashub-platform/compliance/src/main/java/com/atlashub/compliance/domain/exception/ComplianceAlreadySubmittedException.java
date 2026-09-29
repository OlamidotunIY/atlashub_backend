package com.atlashub.compliance.domain.exception;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class ComplianceAlreadySubmittedException extends BusinessRuleException {
    
    public ComplianceAlreadySubmittedException() {
        super("A domain error occurred");
    }

    public ComplianceAlreadySubmittedException(String message) {
        super(message);
    }

    public ComplianceAlreadySubmittedException(String message, Throwable cause) {
        super(message, cause);
    }
}
