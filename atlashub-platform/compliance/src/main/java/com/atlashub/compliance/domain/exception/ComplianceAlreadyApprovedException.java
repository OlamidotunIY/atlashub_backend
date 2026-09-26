package com.atlashub.compliance.domain.exception;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class ComplianceAlreadyApprovedException extends BusinessRuleException {
    
    public ComplianceAlreadyApprovedException() {
        super("A domain error occurred");
    }

    public ComplianceAlreadyApprovedException(String message) {
        super(message);
    }

    public ComplianceAlreadyApprovedException(String message, Throwable cause) {
        super(message, cause);
    }
}
