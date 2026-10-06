package com.atlashub.iam.domain.exception;

import com.atlashub.shared.domain.exception.ConflictException;

public class DuplicateInvitationException extends ConflictException {
    public DuplicateInvitationException() { super("A pending invitation already exists"); }
    public DuplicateInvitationException(String message) { super(message); }
}
