package com.atlashub.accounts.domain.exception;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class EmailAlreadyExistsException extends BusinessRuleException {
    public EmailAlreadyExistsException(String message) { super(message); }
}
