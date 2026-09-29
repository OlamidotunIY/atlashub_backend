package com.atlashub.compliance.domain.exception;

import com.atlashub.shared.domain.exception.ValidationException;

public class InvalidBvnFormatException extends ValidationException {
    
    public InvalidBvnFormatException() {
        super("A domain error occurred");
    }

    public InvalidBvnFormatException(String message) {
        super(message);
    }

    public InvalidBvnFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
