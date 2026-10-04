package com.atlashub.compliance.domain.exception;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidComplianceDataException extends BusinessRuleException {
    public InvalidComplianceDataException(String message) { super(message); }
}
