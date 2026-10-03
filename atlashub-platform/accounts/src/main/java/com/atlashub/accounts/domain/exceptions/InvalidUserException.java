package com.atlashub.accounts.domain.exceptions;

import com.atlashub.shared.domain.exception.ValidationException;

public class InvalidUserException extends ValidationException {
    public InvalidUserException(String message) {
        super(message);
    }
}
