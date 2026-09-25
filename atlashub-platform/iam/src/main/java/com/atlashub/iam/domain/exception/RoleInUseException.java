package com.atlashub.iam.domain.exception;

import com.atlashub.shared.domain.exception.ConflictException;

public class RoleInUseException extends ConflictException {
    
    public RoleInUseException() {
        super("A domain error occurred");
    }

    public RoleInUseException(String message) {
        super(message);
    }

    public RoleInUseException(String message, Throwable cause) {
        super(message, cause);
    }
}
