package com.atlashub.shared.domain.exception;

public class AuthorizationException extends DomainException {
    public AuthorizationException(String message) {
        super(message);
    }
    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}

