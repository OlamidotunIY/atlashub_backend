package com.atlashub.authentication.domain.exceptions;

public class InvalidRedirectUriException extends OAuthException {
    public InvalidRedirectUriException(String description) {
        super("invalid_request", description);
    }

    public InvalidRedirectUriException() {
        this("The requested redirect URI is not registered or not permitted for this client");
    }
}
