package com.atlashub.commerce.storefront.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class TillNotOpenException extends BusinessRuleException {

    public TillNotOpenException() {
        super("Till is not open for transactions");
    }

    public TillNotOpenException(String message) {
        super(message);
    }
}
