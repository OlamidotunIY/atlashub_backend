package com.atlashub.iam.domain.exception;

import com.atlashub.shared.domain.exception.ValidationException;

public class InvalidOrganizationMemberException extends ValidationException {
    public InvalidOrganizationMemberException() { super("Organization member data is invalid"); }
    public InvalidOrganizationMemberException(String message) { super(message); }
}
