package com.atlashub.iam.domain.exception;

import com.atlashub.shared.domain.exception.NotFoundException;

public class OrganizationMemberNotFoundException extends NotFoundException {
    public OrganizationMemberNotFoundException() { super("Organization member was not found"); }
    public OrganizationMemberNotFoundException(String message) { super(message); }
}
