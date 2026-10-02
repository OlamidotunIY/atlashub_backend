package com.atlashub.accounts.domain.exceptions;

import com.atlashub.shared.domain.exception.ValidationException;

public class InvalidOrganizationException extends ValidationException {

    public InvalidOrganizationException() {
        super("A domain error occurred");
    }

    public InvalidOrganizationException(String message) {
        super(message);
    }

    public InvalidOrganizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
