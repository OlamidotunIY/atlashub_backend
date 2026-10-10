package com.atlashub.commerce.catalog.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidPurchaseOrderStateException extends BusinessRuleException {
    public InvalidPurchaseOrderStateException(String message) {
        super(message);
    }
}
