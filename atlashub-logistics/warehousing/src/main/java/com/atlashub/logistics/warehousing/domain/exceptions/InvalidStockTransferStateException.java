package com.atlashub.logistics.warehousing.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidStockTransferStateException extends BusinessRuleException {

    public InvalidStockTransferStateException(String message) {
        super(message);
    }
}
