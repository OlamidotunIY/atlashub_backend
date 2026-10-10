package com.atlashub.commerce.inventory.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidInventoryOperationException extends BusinessRuleException {

    public InvalidInventoryOperationException(String message) {
        super(message);
    }
}
