package com.atlashub.pay.accounts.domain.exceptions;

import com.atlashub.shared.domain.exception.ValidationException;

public class InvalidBankingAccountDataException extends ValidationException {
    public InvalidBankingAccountDataException(String message) { super(message); }
}
