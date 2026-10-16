package com.atlashub.pay.mandates.domain.exceptions;

import com.atlashub.shared.domain.exception.ValidationException;

public class InvalidMandateDetailsException extends ValidationException {

    public InvalidMandateDetailsException(String message) {
        super(message);
    }
}
