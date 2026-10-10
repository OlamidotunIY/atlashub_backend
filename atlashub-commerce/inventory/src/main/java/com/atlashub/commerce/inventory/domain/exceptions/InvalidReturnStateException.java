package com.atlashub.commerce.inventory.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidReturnStateException extends BusinessRuleException {

    public InvalidReturnStateException(String message) {
        super(message);
    }
}
