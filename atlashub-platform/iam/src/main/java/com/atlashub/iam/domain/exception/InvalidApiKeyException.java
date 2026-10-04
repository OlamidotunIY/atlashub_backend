package com.atlashub.iam.domain.exception;

import com.atlashub.shared.domain.exception.ValidationException;

public class InvalidApiKeyException extends ValidationException {
    public InvalidApiKeyException() { super("API key data is invalid"); }
    public InvalidApiKeyException(String message) { super(message); }
}
