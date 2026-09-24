package com.atlashub.shared.domain.exception;

public class ExternalServiceException extends ApplicationException {
    public ExternalServiceException(String message) {
        super(message);
    }
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

