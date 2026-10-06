package com.atlashub.iam.domain.exception;

import com.atlashub.shared.domain.exception.NotFoundException;

public class CustomRoleNotFoundException extends NotFoundException {
    public CustomRoleNotFoundException() { super("Custom role was not found"); }
    public CustomRoleNotFoundException(String message) { super(message); }
}
