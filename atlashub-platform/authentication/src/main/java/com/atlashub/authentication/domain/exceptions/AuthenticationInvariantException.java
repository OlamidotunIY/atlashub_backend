package com.atlashub.authentication.domain.exceptions;

import com.atlashub.shared.domain.exception.ValidationException;

public class AuthenticationInvariantException extends ValidationException {
    public AuthenticationInvariantException(String message) {
        super(message);
    }
}
