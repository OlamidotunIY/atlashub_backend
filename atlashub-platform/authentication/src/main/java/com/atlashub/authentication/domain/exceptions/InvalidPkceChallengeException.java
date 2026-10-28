package com.atlashub.authentication.domain.exceptions;

public class InvalidPkceChallengeException extends OAuthException {
    public InvalidPkceChallengeException(String description) {
        super("invalid_request", description);
    }

    public InvalidPkceChallengeException() {
        this("PKCE verification failed: code verifier does not match challenge or is malformed");
    }
}
