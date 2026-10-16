package com.atlashub.hr.leave.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class LeaveDatesConflictException extends BusinessRuleException {

    public LeaveDatesConflictException() {
        super("An approved or pending leave application already covers these dates");
    }
}
