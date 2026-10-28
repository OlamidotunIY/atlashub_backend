package com.atlashub.authentication.domain.exceptions;

public class InvalidGrantException extends OAuthException {
    public InvalidGrantException(String description) {
        super("invalid_grant", description);
    }

    public InvalidGrantException() {
        this("The provided authorization grant is invalid, expired, revoked, or already used");
    }
}
