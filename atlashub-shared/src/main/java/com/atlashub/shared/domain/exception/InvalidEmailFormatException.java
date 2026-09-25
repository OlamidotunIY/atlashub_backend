package com.atlashub.shared.domain.exception;

public class InvalidEmailFormatException extends ValidationException {
    public InvalidEmailFormatException(String message) { super(message); }
}
