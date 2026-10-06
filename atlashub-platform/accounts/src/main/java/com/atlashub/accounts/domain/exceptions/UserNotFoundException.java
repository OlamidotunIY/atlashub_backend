package com.atlashub.accounts.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(String message) { super(message); }
}
