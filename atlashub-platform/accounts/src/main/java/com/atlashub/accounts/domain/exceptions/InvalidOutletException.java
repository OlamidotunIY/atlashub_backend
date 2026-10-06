package com.atlashub.accounts.domain.exceptions;

import com.atlashub.shared.domain.exception.ValidationException;

public class InvalidOutletException extends ValidationException {
    public InvalidOutletException(String message) {
        super(message);
    }
}
