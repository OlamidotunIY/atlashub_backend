package com.atlashub.hr.staff.domain.exceptions;

import com.atlashub.shared.domain.exception.ConflictException;

public class UserAlreadyEmployedException extends ConflictException {

    public UserAlreadyEmployedException() {
        super("User is already employed in this organization");
    }
}
