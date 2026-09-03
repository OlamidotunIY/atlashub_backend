package com.atlashub.shared.domain.exception;

public abstract class ApplicationException extends AtlasHubException {

    protected ApplicationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    protected ApplicationException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
