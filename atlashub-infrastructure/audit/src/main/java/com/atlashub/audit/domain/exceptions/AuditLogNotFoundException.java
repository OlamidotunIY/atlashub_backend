package com.atlashub.audit.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class AuditLogNotFoundException extends NotFoundException {

    public AuditLogNotFoundException(Long id) {
        super("Audit log not found with ID: " + id);
    }
}
