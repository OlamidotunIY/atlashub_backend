package com.atlashub.shared.domain.exception;

public abstract class DomainException extends AtlasHubException {
    protected DomainException(String message) {
        super(message);
    }
    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
