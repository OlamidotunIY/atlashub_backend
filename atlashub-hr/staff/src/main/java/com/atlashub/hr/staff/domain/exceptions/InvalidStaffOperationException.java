package com.atlashub.hr.staff.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidStaffOperationException extends BusinessRuleException {

    public InvalidStaffOperationException(String message) {
        super(message);
    }
}
