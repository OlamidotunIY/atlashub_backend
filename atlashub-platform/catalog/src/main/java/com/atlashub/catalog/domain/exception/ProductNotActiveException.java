package com.atlashub.catalog.domain.exception;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class ProductNotActiveException extends BusinessRuleException {
    public ProductNotActiveException(String message) {
        super(message);
    }

    public ProductNotActiveException(String message, Throwable cause) {
        super(message, cause);
    }
}
