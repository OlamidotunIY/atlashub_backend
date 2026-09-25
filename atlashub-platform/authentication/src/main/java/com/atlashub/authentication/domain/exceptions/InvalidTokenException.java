package com.atlashub.authentication.domain.exceptions;

import com.atlashub.shared.domain.exception.AuthorizationException;

public class InvalidTokenException extends AuthorizationException {
    public InvalidTokenException() {
        super("No active session");
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
