package com.atlashub.accounts.domain.exceptions;

import com.atlashub.shared.domain.exception.ValidationException;

public class WeakPasswordException extends ValidationException {
    public WeakPasswordException(String message) {
        super(message);
    }
}
