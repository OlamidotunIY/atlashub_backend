package com.atlashub.pay.txquery.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class TransactionRecordNotFoundException extends NotFoundException {
    public TransactionRecordNotFoundException(String value) {
        super("Transaction record not found: " + value);
    }
}
