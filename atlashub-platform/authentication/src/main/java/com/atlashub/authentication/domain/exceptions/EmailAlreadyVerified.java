package com.atlashub.authentication.domain.exceptions;

import com.atlashub.shared.domain.exception.ConflictException;

public class EmailAlreadyVerified extends ConflictException {
    public EmailAlreadyVerified() {
        super("Email is already verified");
    }

    public EmailAlreadyVerified(String message, Throwable cause) {
        super(message, cause);
    }
}
