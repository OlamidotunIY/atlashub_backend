package com.atlashub.iam.domain.exception;

import com.atlashub.shared.domain.exception.NotFoundException;

public class ApiKeyNotFoundException extends NotFoundException {
    public ApiKeyNotFoundException() { super("API key was not found"); }
    public ApiKeyNotFoundException(String message) { super(message); }
}
