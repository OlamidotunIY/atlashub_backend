package com.atlashub.authentication.domain.exceptions;

public class InvalidScopeException extends OAuthException {
    public InvalidScopeException(String description) {
        super("invalid_scope", description);
    }

    public InvalidScopeException() {
        this("The requested scope is invalid, unknown, or exceeds permitted scopes");
    }
}
