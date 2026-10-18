package com.atlashub.pay.transfers.domain.exceptions;

import com.atlashub.shared.domain.exception.ConflictException;

public class DuplicateReferenceException extends ConflictException {

    public DuplicateReferenceException() {
        super("A payout with this reference already exists");
    }

    public DuplicateReferenceException(String message) {
        super(message);
    }
}
