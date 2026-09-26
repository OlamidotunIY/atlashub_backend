package com.atlashub.compliance.domain.exception;

import com.atlashub.shared.domain.exception.ValidationException;

public class InvalidNinFormatException extends ValidationException {
    
    public InvalidNinFormatException() {
        super("A domain error occurred");
    }

    public InvalidNinFormatException(String message) {
        super(message);
    }

    public InvalidNinFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
