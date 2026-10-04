package com.atlashub.iam.domain.exception;

import com.atlashub.shared.domain.exception.ValidationException;

public class InvalidCustomRoleException extends ValidationException {
    public InvalidCustomRoleException() { super("Custom role data is invalid"); }
    public InvalidCustomRoleException(String message) { super(message); }
}
