package com.atlashub.shared.domain.exception;

public class NotFoundException extends DomainException {

    public NotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public NotFoundException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
