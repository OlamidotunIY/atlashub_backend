package com.atlashub.shared.domain.exception;

public class MissingRequiredFieldException extends ValidationException {
    public MissingRequiredFieldException(String message) { super(message); }
}
