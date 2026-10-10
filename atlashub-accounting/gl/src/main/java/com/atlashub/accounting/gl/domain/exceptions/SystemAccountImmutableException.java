package com.atlashub.accounting.gl.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class SystemAccountImmutableException extends BusinessRuleException {

    public SystemAccountImmutableException() {
        super("System accounts cannot be modified or deleted");
    }
}
