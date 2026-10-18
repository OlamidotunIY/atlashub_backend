package com.atlashub.support.domain.exceptions;

import com.atlashub.shared.domain.exception.AuthorizationException;

public class UnauthorizedTicketActionException extends AuthorizationException {
    public UnauthorizedTicketActionException(String message) {
        super(message);
    }
}
