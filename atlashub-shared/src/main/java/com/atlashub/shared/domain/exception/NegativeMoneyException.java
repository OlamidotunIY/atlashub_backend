package com.atlashub.shared.domain.money;

import com.atlashub.shared.domain.exception.ValidationException;

public class NegativeMoneyException extends ValidationException {
    public NegativeMoneyException(String message, Throwable cause) {
        super(message, cause);
    }

    public NegativeMoneyException(String message) {
        super(message);
    }
}
