package com.atlashub.commerce.storefront.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidOrderStateException extends BusinessRuleException {

    public InvalidOrderStateException(String message) {
        super(message);
    }
}
