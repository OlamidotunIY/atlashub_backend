package com.atlashub.pay.transfers.domain.exceptions;

import com.atlashub.shared.domain.exception.ValidationException;

public class InvalidBankDetailsException extends ValidationException {

    public InvalidBankDetailsException() {
        super("Invalid recipient bank details");
    }

    public InvalidBankDetailsException(String message) {
        super(message);
    }
}
