package com.atlashub.accounting.gl.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class AccountNotFoundException extends NotFoundException {

    public AccountNotFoundException(Long id) {
        super("Account not found: " + id);
    }

    public AccountNotFoundException(String code) {
        super("Account not found with code: " + code);
    }
}
