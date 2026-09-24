package com.atlashub.shared.domain.exception;

public class NegativeMoneyException extends ValidationException {
    public NegativeMoneyException(String message, Throwable cause) {
        super(message, cause);
    }

    public NegativeMoneyException(String message) {
        super(message);
    }
}
