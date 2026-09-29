package com.atlashub.compliance.domain.exception;

import com.atlashub.shared.domain.exception.NotFoundException;

public class ComplianceRecordNotFoundException extends NotFoundException {
    
    public ComplianceRecordNotFoundException() {
        super("A domain error occurred");
    }

    public ComplianceRecordNotFoundException(String message) {
        super(message);
    }

    public ComplianceRecordNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
