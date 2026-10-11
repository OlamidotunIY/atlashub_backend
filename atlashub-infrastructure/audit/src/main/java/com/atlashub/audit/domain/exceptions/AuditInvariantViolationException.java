package com.atlashub.audit.domain.exceptions;

import com.atlashub.shared.domain.exception.DomainException;

public class AuditInvariantViolationException extends DomainException {

    public AuditInvariantViolationException(String message) {
        super(message);
    }
}
