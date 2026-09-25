package com.atlashub.accounts.domain.exception;

import com.atlashub.shared.domain.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(String message) { super(message); }
}
