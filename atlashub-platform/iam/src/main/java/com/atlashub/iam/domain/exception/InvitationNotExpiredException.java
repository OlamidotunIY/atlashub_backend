package com.atlashub.iam.domain.exception;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvitationNotExpiredException extends BusinessRuleException {
    public InvitationNotExpiredException() { super("Invitation has not expired"); }
    public InvitationNotExpiredException(String message) { super(message); }
}
