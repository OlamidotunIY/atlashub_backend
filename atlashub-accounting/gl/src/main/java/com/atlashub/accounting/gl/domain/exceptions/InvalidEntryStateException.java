package com.atlashub.accounting.gl.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidEntryStateException extends BusinessRuleException {

    public InvalidEntryStateException(String message) {
        super(message);
    }
}
