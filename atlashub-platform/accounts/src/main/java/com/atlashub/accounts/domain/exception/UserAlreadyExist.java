package com.atlashub.accounts.domain.exception;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class UserAlreadyExist extends BusinessRuleException {
    public UserAlreadyExist(String message) {
        super(message);
    }

    public UserAlreadyExist(String message, Throwable cause) {
        super(message, cause);
    }
}
