package com.atlashub.authentication.domain.exceptions;

import com.atlashub.shared.domain.exception.ValidationException;

public class AuthInvaraintError extends ValidationException {
    public AuthInvaraintError(String message) {
        super(message);
    }

    public AuthInvaraintError(String message, Throwable cause) {
        super(message, cause);
    }
}
