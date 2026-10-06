package com.atlashub.accounts.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidOutletStateException extends BusinessRuleException {

    public InvalidOutletStateException() {
        super("A domain error occurred");
    }

    public InvalidOutletStateException(String message) {
        super(message);
    }

    public InvalidOutletStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
