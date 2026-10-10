package com.atlashub.commerce.inventory.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidTransferStateException extends BusinessRuleException {

    public InvalidTransferStateException(String message) {
        super(message);
    }
}
