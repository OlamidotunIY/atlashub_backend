package com.atlashub.shared.domain.exception;

public class UnsupportedCountryException extends ValidationException {
    public UnsupportedCountryException(String message) {
        super(message);
    }

    public UnsupportedCountryException(String message, Throwable cause) {
        super(message, cause);
    }
}
