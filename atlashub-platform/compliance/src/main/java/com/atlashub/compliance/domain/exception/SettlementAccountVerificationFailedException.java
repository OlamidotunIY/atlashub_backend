package com.atlashub.compliance.domain.exception;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class SettlementAccountVerificationFailedException extends BusinessRuleException {
    
    public SettlementAccountVerificationFailedException() {
        super("A domain error occurred");
    }

    public SettlementAccountVerificationFailedException(String message) {
        super(message);
    }

    public SettlementAccountVerificationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
