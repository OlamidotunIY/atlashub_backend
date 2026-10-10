package com.atlashub.accounting.gl.domain.exceptions;

import com.atlashub.shared.domain.exception.ConflictException;

public class DuplicateAccountCodeException extends ConflictException {

    public DuplicateAccountCodeException(String code) {
        super("Account code already exists: " + code);
    }
}
