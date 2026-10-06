package com.atlashub.iam.domain.exception;

import com.atlashub.shared.domain.exception.AuthorizationException;

public class InvalidInvitationRecipientException extends AuthorizationException {
    public InvalidInvitationRecipientException() { super("Invitation does not belong to this user"); }
    public InvalidInvitationRecipientException(String message) { super(message); }
}
