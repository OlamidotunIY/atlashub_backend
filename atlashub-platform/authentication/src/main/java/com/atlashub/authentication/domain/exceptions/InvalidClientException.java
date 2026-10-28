package com.atlashub.authentication.domain.exceptions;

public class InvalidClientException extends OAuthException {
    public InvalidClientException(String description) {
        super("invalid_client", description);
    }

    public InvalidClientException() {
        this("Client authentication failed or client is invalid or disabled");
    }
}
