package com.atlashub.shared.domain.exception;

public abstract class ApplicationException extends AtlasHubException {
    protected ApplicationException(String message) {
        super(message);
    }
    protected ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}

