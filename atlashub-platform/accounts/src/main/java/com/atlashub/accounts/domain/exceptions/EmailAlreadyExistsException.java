package com.atlashub.accounts.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class EmailAlreadyExistsException extends BusinessRuleException {
    public EmailAlreadyExistsException(String message) { super(message); }
}
