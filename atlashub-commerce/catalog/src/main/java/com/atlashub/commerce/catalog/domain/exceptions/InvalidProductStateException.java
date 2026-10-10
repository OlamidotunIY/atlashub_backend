package com.atlashub.commerce.catalog.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidProductStateException extends BusinessRuleException {
    public InvalidProductStateException(String message) {
        super(message);
    }
}
