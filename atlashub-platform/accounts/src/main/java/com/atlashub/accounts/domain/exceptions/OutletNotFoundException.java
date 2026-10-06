package com.atlashub.accounts.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class OutletNotFoundException extends NotFoundException {
    public OutletNotFoundException(String message) { super(message); }
}
