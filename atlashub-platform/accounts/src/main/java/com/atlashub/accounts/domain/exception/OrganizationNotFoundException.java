package com.atlashub.accounts.domain.exception;

import com.atlashub.shared.domain.exception.NotFoundException;

public class OrganizationNotFoundException extends NotFoundException {
    public OrganizationNotFoundException(String message) { super(message); }
}
