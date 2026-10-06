package com.atlashub.pay.splits.domain.exceptions;

import com.atlashub.shared.domain.exception.ValidationException;

public class MissingSubaccountException extends ValidationException {
    public MissingSubaccountException() {
        super("A split rule must have at least one subaccount");
    }

    public MissingSubaccountException(String message) {
        super(message);
    }
}
