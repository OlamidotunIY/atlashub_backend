package com.atlashub.accounting.gl.domain.exceptions;

import com.atlashub.shared.domain.exception.AuthorizationException;

public class SelfApprovalNotAllowedException extends AuthorizationException {

    public SelfApprovalNotAllowedException() {
        super("The approver cannot be the same person who initiated the journal entry");
    }
}
