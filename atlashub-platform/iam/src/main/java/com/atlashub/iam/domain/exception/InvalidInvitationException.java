package com.atlashub.iam.domain.exception;

import com.atlashub.shared.domain.exception.ValidationException;

public class InvalidInvitationException extends ValidationException {
    public InvalidInvitationException() { super("Invitation data is invalid"); }
    public InvalidInvitationException(String message) { super(message); }
}
