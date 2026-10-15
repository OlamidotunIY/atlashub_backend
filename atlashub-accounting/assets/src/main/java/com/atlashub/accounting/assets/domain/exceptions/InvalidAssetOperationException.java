package com.atlashub.accounting.assets.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidAssetOperationException extends BusinessRuleException {

    public InvalidAssetOperationException(String message) {
        super(message);
    }
}
