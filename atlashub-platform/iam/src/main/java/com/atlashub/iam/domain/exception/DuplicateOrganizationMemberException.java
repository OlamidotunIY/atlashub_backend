package com.atlashub.iam.domain.exception;

import com.atlashub.shared.domain.exception.ConflictException;

public class DuplicateOrganizationMemberException extends ConflictException {
    public DuplicateOrganizationMemberException() { super("User is already an organization member"); }
    public DuplicateOrganizationMemberException(String message) { super(message); }
}
