package com.atlashub.authentication.domain.exceptions;

import com.atlashub.shared.domain.exception.DomainException;

public class InvalidCredentials extends DomainException {
    public InvalidCredentials() {
        super("Email or Password incorrect, please try again");
    }

    public InvalidCredentials(String message, Throwable cause) {
        super(message, cause);
    }
}
