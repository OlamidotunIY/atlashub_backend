package com.atlashub.audit.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class AuditLogImmutableException extends BusinessRuleException {

    public AuditLogImmutableException() {
        super("Audit logs are append-only and cannot be modified or deleted");
    }
}
