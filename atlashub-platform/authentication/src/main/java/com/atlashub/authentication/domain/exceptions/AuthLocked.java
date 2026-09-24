package com.atlashub.authentication.domain.exceptions;

import com.atlashub.shared.domain.exception.DomainException;

public class AuthLocked extends DomainException {
    public AuthLocked(String lockedTill) {
        super("Account is locked until: %s".formatted(lockedTill));
    }

    public AuthLocked() {
        super("This account has being locked, wait a few minutes and try again");
    }

    public AuthLocked(String message, Throwable cause) {
        super(message, cause);
    }
}
