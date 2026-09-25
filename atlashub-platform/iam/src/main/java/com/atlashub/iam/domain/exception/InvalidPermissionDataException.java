package com.atlashub.iam.domain.exception;

import com.atlashub.shared.domain.exception.ValidationException;

public class InvalidPermissionDataException extends ValidationException {
    
    public InvalidPermissionDataException() {
        super("A domain error occurred");
    }

    public InvalidPermissionDataException(String message) {
        super(message);
    }

    public InvalidPermissionDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
