package com.atlashub.iam.domain.exception;

import com.atlashub.shared.domain.exception.NotFoundException;

public class InvitationNotFoundException extends NotFoundException {
    public InvitationNotFoundException() { super("Invitation was not found"); }
    public InvitationNotFoundException(String message) { super(message); }
}
