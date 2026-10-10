package com.atlashub.pay.txquery.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidTransactionRecordException extends BusinessRuleException {
    public InvalidTransactionRecordException(String message) {
        super(message);
    }
}
