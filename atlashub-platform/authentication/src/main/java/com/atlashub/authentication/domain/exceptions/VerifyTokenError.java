package com.atlashub.authentication.domain.exceptions;

import com.atlashub.shared.domain.exception.ConflictException;

public class VerifyTokenError extends ConflictException {
    public VerifyTokenError() {
        super("Token cannot be verified it is either revoked or used");
    }

    public VerifyTokenError(String message) {
        super(message);
    }

    public VerifyTokenError(String message, Throwable cause) {
        super(message, cause);
    }
}
