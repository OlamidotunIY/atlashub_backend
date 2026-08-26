package com.atlashub.shared.exception;

public abstract class DomainException extends AtlasHubException {

    protected DomainException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    protected DomainException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
