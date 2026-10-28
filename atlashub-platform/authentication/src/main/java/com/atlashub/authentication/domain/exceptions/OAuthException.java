package com.atlashub.authentication.domain.exceptions;

import com.atlashub.shared.domain.exception.AuthorizationException;

public class OAuthException extends AuthorizationException {
    private final String error;

    public OAuthException(String error, String description) {
        super(description);
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
