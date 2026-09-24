package com.atlashub.accounts.domain.exception;

import com.atlashub.shared.domain.exception.ValidationException;

public class WeakPasswordException extends ValidationException {
    public WeakPasswordException(String message) {
        super(message);
    }
}
