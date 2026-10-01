package com.atlashub.pay.accounts.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class VirtualAccountNotFoundException extends NotFoundException {
    public VirtualAccountNotFoundException() { super("Virtual account not found"); }
    public VirtualAccountNotFoundException(String message) { super(message); }
}

